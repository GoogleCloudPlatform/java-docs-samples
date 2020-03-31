package com.example.cloudrun;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=EditorApplication.class)
@WebMvcTest(EditorController.class)
class EditorControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void getIndex() throws Exception {
		this.mockMvc.perform(get("/"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("<title>Markdown Editor</title>")))
			.andExpect(content().string(containsString("This UI allows a user to write Markdown text and preview the rendered HTML.")));
	}

}
