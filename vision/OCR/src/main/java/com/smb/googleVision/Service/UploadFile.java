/*
 * @author karansharma6190@gmail.com
 */

package com.smb.googleVision.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Part;

import org.springframework.stereotype.Component;

import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Component
public class UploadFile {

	private Vision vision = new Vision();
	static Storage storage = null;
	static {
		storage = StorageOptions.getDefaultInstance().getService();
	}

	@SuppressWarnings("deprecation")
	public String uploadFile(Part filePart, String bucketName) throws Exception {

		String image = filePart.getSubmittedFileName();

		BlobInfo blobInfo = storage.create(
				BlobInfo.newBuilder(bucketName, image).setContentType("image/jpeg")
						// Modify access list to allow all users with link to
						// read file
						.setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER)))).build(),
				filePart.getInputStream());
		// return the public download link
		return vision.GoogleVision(image);
	}

}

	
class Vision {
	public String GoogleVision(String image) throws Exception,
	IOException {
	    // Instantiates a client
	  List<AnnotateImageRequest> requests = new ArrayList<>();
	  String gcsbucket="gs://*********/;    //GCP_BUCKET_NAME"
	  String gcsPath= gcsbucket+image;
	    

	  ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
	  Image img = Image.newBuilder().setSource(imgSource).build();
	  Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
	  AnnotateImageRequest request =
	      AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
	  requests.add(request);

	 StringBuilder responseBuilder = new StringBuilder();

	  try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
	    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
	    List<AnnotateImageResponse> responses = response.getResponsesList();

	    for (AnnotateImageResponse res : responses) {
	      if (res.hasError()) {
	    	//System.out.printf("Error: %s\n", res.getError().getMessage());
	    	return res.getError().getMessage();
	        
	      }
	      
	      responseBuilder.append(res.getTextAnnotations(0).getDescription()); 
	    }
	  }
          return responseBuilder.toString();
    }
}	
