/*
 * @author karansharma6190@gmail.com
 */

package com.smb.googleVision.Controller;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.smb.googleVision.Service.UploadFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BucketUpload {
	
	@Autowired
	private UploadFile file;
	
	
	@PostMapping("/upload/")
	  public String uploadFile( HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String bucketName="GCP_BUCKET_NAME";
		Part filePart = req.getPart("file");
		  final String fileName = filePart.getSubmittedFileName();
		  String imageUrl = req.getParameter("imageUrl");
		  // Check extension of file
		  if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
		    final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		    String[] allowedExt = {"jpg", "jpeg", "png"};
		    for (String s : allowedExt) {
		      if (extension.equals(s)) {
		        return file.uploadFile(filePart, bucketName);
		      }
		    }
		    try {
				throw new ServletException("file must be an image");
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  return imageUrl;
   }
}	
