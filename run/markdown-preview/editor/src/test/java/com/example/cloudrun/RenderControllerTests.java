package com.example.cloudrun;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=EditorApplication.class)
@WebMvcTest(RenderController.class)
class RednerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void postRender() throws Exception {
		String mock = "{\"data\":\"test\"}";
		this.mockMvc.perform(post("/render")
		.contentType(MediaType.APPLICATION_JSON)
		.content(mock))
		.andExpect(status().isOk());
	}

	@Test
	public void failsRenderWithInvalidMedia() throws Exception {
		this.mockMvc.perform(post("/render")).andExpect(status().isUnsupportedMediaType());
	}

	@Test
	public void failsGetRender() throws Exception {
		this.mockMvc.perform(get("/render")).andExpect(status().isMethodNotAllowed());
	}


}
