package com.cg.osce.apibuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cg.osce.apibuilder.pojo.Constants;
import com.cg.osce.apibuilder.pojo.SwaggerSchema;
import com.cg.osce.apibuilder.pojo.SwaggerSchema.Info;
import com.cg.osce.apibuilder.service.IxmlSchema2yaml;
import com.cg.osce.apibuilder.storage.StorageFileNotFoundException;
import com.cg.osce.apibuilder.storage.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

@Controller
public class APIBuilderController {

	private final StorageService storageService;

	@Autowired
	IxmlSchema2yaml ixmlSchema2yaml;

	private static final Logger LOGGER = LoggerFactory.getLogger(APIBuilderController.class);

	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));

	static final JsonNodeFactory factory = JsonNodeFactory.instance;

	@Autowired
	public APIBuilderController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/asd")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files",
				storageService.loadAll()
						.map(path -> MvcUriComponentsBuilder
								.fromMethodName(APIBuilderController.class, "serveFile", path.getFileName().toString())
								.build().toString())
						.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/")
	public String getAPIDocument(Model model) throws IOException {

		Path path = storageService.load("APIDocument.yaml");
		model.addAttribute("file",
				MvcUriComponentsBuilder
						.fromMethodName(APIBuilderController.class, "serveFile", path.getFileName().toString()).build()
						.toString());

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@ModelAttribute FormWrapper formWrapper, @RequestParam("file") MultipartFile file,
			@RequestParam("host") String host, RedirectAttributes redirectAttributes) throws IOException {

		String[] command = { "cmd", };
		Process p;
		try {

			p = Runtime.getRuntime().exec(command);
			new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
			new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
			PrintWriter stdin = new PrintWriter(p.getOutputStream());
			stdin.println("hostname");

			stdin.println("xjc -d src/main/java/com/osce/api_builder/xsd -p entity "
					+ formWrapper.getFile().getOriginalFilename());
			stdin.close();
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		printYAML(formWrapper);
		
		redirectAttributes.addFlashAttribute("message",
				"API Document generated successfully from " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	public SwaggerSchema printYAML(FormWrapper request) throws IOException {

		SwaggerSchema obj = new SwaggerSchema();
		obj.setSwagger(request.getSwaggerVersion());
		Info info = new Info();
		info.setVersion(request.getSwaggerVersion());

		info.setTitle(request.getTitle());
		info.setDescription(request.getDescription());
		obj.setInfo(info);
		obj.setBasePath(request.getBasepath());
		obj.setHost(request.getHost());
		Set<Class<? extends Object>> entities = ixmlSchema2yaml.getClassDetails();

		ObjectNode paths = factory.objectNode();
		for (Class<? extends Object> entity : entities) {
			if ("ObjectFactory".equals(entity.getSimpleName()) || "package-info".equals(entity.getSimpleName()))
				continue;

			ObjectNode path = factory.objectNode();

			path.putPOJO("get", ixmlSchema2yaml.createDefaultGet(entity, factory));
			path.putPOJO("post", ixmlSchema2yaml.createDefaultPost(entity, factory));
			path.putPOJO("put", ixmlSchema2yaml.createDefaultPut(entity, factory));
			path.putPOJO("delete", ixmlSchema2yaml.createDefaultDelete(entity, factory));
			paths.set("SLASH" + entity.getSimpleName().toLowerCase(), path);
		}

		obj.setPaths(paths);

		obj.setDefinitions(ixmlSchema2yaml.creteDefinitions(factory));
		try {
			FileWriter fw = new FileWriter(Constants.FILELOCATION);
			fw.write(mapper.writeValueAsString(obj).replace("SLASH", "/"));
			fw.close();
		} catch (Exception e) {
			LOGGER.error(e.toString());
		}
		return obj;

	}

	public static String getYAML(Object obj) throws JsonProcessingException {
		return mapper.writeValueAsString(obj);
	}

}
