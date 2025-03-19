package org.mskcc.cbio.oncokb.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {


	@RequestMapping(value="/**")
	public ModelAndView test(HttpServletResponse response) throws IOException{
		return new ModelAndView("redirect:/api/v1/swagger-ui.html");
	}
}
