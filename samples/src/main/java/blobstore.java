//[START uploading_a_blob_1]
<body>
<form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
<input type="file" name="myFile">
<input type="submit" value="Submit">
</form>
</body>
// [END uploading_a_blob_1]

// [START uploading_a_blob_2]
Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
List<BlobKey> blobKeys = blobs.get("myFile");

if (blobKeys == null || blobKeys.isEmpty()) {
	res.sendRedirect("/");
} else {
	res.sendRedirect("/serve?blob-key=" + blobKeys.get(0).getKeyString());
}
// [END uploading_a_blob_2]

// [START serving_a_blob]
public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws IOException {
	BlobKey blobKey = new BlobKey(req.getParameter("blob-key"));
	blobstoreService.serve(blobKey, res);
	// [END serving_a_blob]

	// [START complete_sample_application]
	// file Upload.java

	import java.io.IOException;
	import java.util.List;
	import java.util.Map;

	import javax.servlet.ServletException;
	import javax.servlet.http.HttpServlet;
	import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;

	import com.google.appengine.api.blobstore.BlobKey;
	import com.google.appengine.api.blobstore.BlobstoreService;
	import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

	public class Upload extends HttpServlet {
		private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

		@Override
		public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {

		Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
		List<BlobKey> blobKeys = blobs.get("myFile");

		if (blobKeys == null || blobKeys.isEmpty()) {
			res.sendRedirect("/");
		} else {
			res.sendRedirect("/serve?blob-key=" + blobKeys.get(0).getKeyString());
		}
		}
	}

	// file Serve.java

	import java.io.IOException;

	import javax.servlet.http.HttpServlet;
	import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;

	import com.google.appengine.api.blobstore.BlobKey;
	import com.google.appengine.api.blobstore.BlobstoreService;
	import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

	public class Serve extends HttpServlet {
		private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws IOException {
		BlobKey blobKey = new BlobKey(req.getParameter("blob-key"));
		blobstoreService.serve(blobKey, res);
		}
	}


	// file index.jsp

	<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
		<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>

		<%
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	%>


		<html>
		<head>
		<title>Upload Test</title>
		</head>
		<body>
		<form action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
		<input type="text" name="foo">
		<input type="file" name="myFile">
		<input type="submit" value="Submit">
		</form>
		</body>
		</html>

		// web.xml

		<?xml version="1.0" encoding="utf-8"?>
		<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns="http://java.sun.com/xml/ns/javaee"
		xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
		xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
		http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" version="2.5">

		<servlet>
		<servlet-name>Upload</servlet-name>
		<servlet-class>Upload</servlet-class>
		</servlet>

		<servlet>
		<servlet-name>Serve</servlet-name>
		<servlet-class>Serve</servlet-class>
		</servlet>

		<servlet-mapping>
		<servlet-name>Upload</servlet-name>
		<url-pattern>/upload</url-pattern>
		</servlet-mapping>

		<servlet-mapping>
		<servlet-name>Serve</servlet-name>
		<url-pattern>/serve</url-pattern>
		</servlet-mapping>

		</web-app>
		// [END complete_sample_application]

		// [START writing_files_to_the_Blobstore]
		// Get a file service
		FileService fileService = FileServiceFactory.getFileService();

	// Create a new Blob file with mime-type "text/plain"
	AppEngineFile file = fileService.createNewBlobFile("text/plain");

	// Open a channel to write to it
	boolean lock = false;
	FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);

	// Different standard Java ways of writing to the channel
	// are possible. Here we use a PrintWriter:
	PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
	out.println("The woods are lovely dark and deep.");
	out.println("But I have promises to keep.");

	// Close without finalizing and save the file path for writing later
	out.close();
	String path = file.getFullPath();

	// Write more to the file in a separate request:
	file = new AppEngineFile(path);

	// This time lock because we intend to finalize
	lock = true;
	writeChannel = fileService.openWriteChannel(file, lock);

	// This time we write to the channel directly
	writeChannel.write(ByteBuffer.wrap
			("And miles to go before I sleep.".getBytes()));

	// Now finalize
	writeChannel.closeFinally();

	// Later, read from the file using the Files API
	lock = false; // Let other people read at the same time
	FileReadChannel readChannel = fileService.openReadChannel(file, false);

	// Again, different standard Java ways of reading from the channel.
	BufferedReader reader =
		new BufferedReader(Channels.newReader(readChannel, "UTF8"));
	String line = reader.readLine();
	// line = "The woods are lovely dark and deep."

	readChannel.close();

	// Now read from the file using the Blobstore API
	BlobKey blobKey = fileService.getBlobKey(file);
	BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
	String segment = new String(blobStoreService.fetchData(blobKey, 30, 40));
	// [END writing_files_to_the_Blobstore]
