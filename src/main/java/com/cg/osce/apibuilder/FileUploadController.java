package com.cg.osce.apibuilder;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.stream.Collectors;

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

import com.cg.osce.apibuilder.storage.StorageFileNotFoundException;
import com.cg.osce.apibuilder.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/asd")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files",
				storageService.loadAll()
						.map(path -> MvcUriComponentsBuilder
								.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
								.build().toString())
						.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/")
	public String getAPIDocument(Model model) throws IOException {

		Path path = storageService.load("APIDocument.yaml");
		model.addAttribute("file",
				MvcUriComponentsBuilder
						.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString()).build()
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
			@RequestParam("host") String host, RedirectAttributes redirectAttributes) {

		System.err.println(formWrapper.getBasepath());
		System.err.println(formWrapper.getDescription());
		System.err.println(formWrapper.getHost());
		System.err.println(formWrapper.getTitle());
		storageService.store(formWrapper.getFile());
		System.out.println(formWrapper.getFile().getOriginalFilename());
		/*try {
			try {
				FileWriter documetnWriter = new FileWriter("src/main/resources/apiDocument/apiDocument.yaml");
				documetnWriter.write("API Document");
				documetnWriter.close();
				FileWriter commandWriter = new FileWriter("src/main/resources/command/cmd.exe");
				commandWriter.write("xjc -d src/main/java -p com.cg.osce.api_builder.entity  xsd/"
						+ formWrapper.getFile().getOriginalFilename());
				commandWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("cmd xjc -d src/main/java -p com.cg.osce.api_builder.entity  xsd/"
					+ formWrapper.getFile().getOriginalFilename());
			//
			// Process pr = rt.exec("C:\\Users\\vs40\\command.exe");

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String line = null;

			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}

			int exitVal = pr.waitFor();
			System.out.println("Exited with error code " + exitVal);

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}*/
		
		String[] command =
		    {
		        "cmd",
		    };
		    Process p;
			try {
				FileWriter documetnWriter = new FileWriter("src/main/resources/apiDocument/apiDocument.yaml");
				documetnWriter.write("API Document");
				documetnWriter.close();
				
				p = Runtime.getRuntime().exec(command);
			        new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
		                new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
		                PrintWriter stdin = new PrintWriter(p.getOutputStream());
		                stdin.println("hostname");

		                stdin.println("xjc -d src/main/java/com/osce/api_builder/xsd -p entity "+ formWrapper.getFile().getOriginalFilename());
		                stdin.close();
		                p.waitFor();
		    	} catch (Exception e) {
		 		e.printStackTrace();
			}
	
		redirectAttributes.addFlashAttribute("message",
				"API Document generated successfully from " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
