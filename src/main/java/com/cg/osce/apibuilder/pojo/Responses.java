
package com.cg.osce.apibuilder.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "200",
    "400",
    "404",
    "405"
})
public class Responses {

    @JsonProperty("200")
    private com.cg.osce.apibuilder.pojo._200 _200;
    @JsonProperty("400")
    private com.cg.osce.apibuilder.pojo._400 _400;
    @JsonProperty("404")
    private com.cg.osce.apibuilder.pojo._404 _404;
    @JsonProperty("405")
    private com.cg.osce.apibuilder.pojo._405 _405;

    public com.cg.osce.apibuilder.pojo._200 get_200() {
		return _200;
	}

	public void set_200(com.cg.osce.apibuilder.pojo._200 _200) {
		this._200 = _200;
	}

	public com.cg.osce.apibuilder.pojo._400 get_400() {
		return _400;
	}

	public void set_400(com.cg.osce.apibuilder.pojo._400 _400) {
		this._400 = _400;
	}

	public com.cg.osce.apibuilder.pojo._404 get_404() {
		return _404;
	}

	public void set_404(com.cg.osce.apibuilder.pojo._404 _404) {
		this._404 = _404;
	}

	public com.cg.osce.apibuilder.pojo._405 get_405() {
		return _405;
	}

	public void set_405(com.cg.osce.apibuilder.pojo._405 _405) {
		this._405 = _405;
	}

	@JsonProperty("200")
    public com.cg.osce.apibuilder.pojo._200 get200() {
        return _200;
    }

    @JsonProperty("200")
    public void set200(com.cg.osce.apibuilder.pojo._200 _200) {
        this._200 = _200;
    }

    @JsonProperty("400")
    public com.cg.osce.apibuilder.pojo._400 get400() {
        return _400;
    }

    @JsonProperty("400")
    public void set400(com.cg.osce.apibuilder.pojo._400 _400) {
        this._400 = _400;
    }

}
