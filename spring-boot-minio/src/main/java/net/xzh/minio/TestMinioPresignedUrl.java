package net.xzh.minio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * 测试通过预签名上传下载文件
 * @author xzh
 *
 */
public class TestMinioPresignedUrl {
	public static void main(String[] args) {
		try {
			MinioPresignedService service = new MinioPresignedService("http://172.17.17.194:9009",
					"minioadmin", "minioadmin", "shang");

			// 生成一个 5 分钟有效期的上传 URL
			String fileName = System.currentTimeMillis() + ".jpg";
			String uploadUrl = service.getUploadUrl("uploads/" + fileName, 300);
			System.out.println("预签名上传 URL: " + uploadUrl);
			uploadFile(uploadUrl);
			
			// 生成一个 5 分钟有效期的下载 URL
			String downloadUrl = service.getDownloadUrl("uploads/1791.jpg", 300);
			System.out.println("预签名下载 URL: " + downloadUrl);
            downloadFile(downloadUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 上传文件
	 * @param uploadUrl
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws FileNotFoundException
	 */
	private static void uploadFile(String uploadUrl)
			throws IOException, MalformedURLException, ProtocolException, FileNotFoundException {
		// 上传文件
		File file = new File("C:\\Users\\CR7\\Pictures\\1791.jpg");
		HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type", "image/jpeg"); // 根据文件类型设置
		try (FileInputStream fis = new FileInputStream(file); OutputStream os = connection.getOutputStream()) {
			byte[] buffer = new byte[8192];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			os.flush();
		}
		int responseCode = connection.getResponseCode();
		if (responseCode == 200) {
			System.out.println("上传成功");
		} else {
			System.out.println("上传失败，响应码：" + responseCode);
		}
		connection.disconnect();
	}
	
	/**
	 * 下载文件
	 * @param downloadUrl
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws FileNotFoundException
	 */
	private static void downloadFile(String downloadUrl)
			throws MalformedURLException, IOException, ProtocolException, FileNotFoundException {
		URL url = new URL(downloadUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
		    // 将响应内容保存到本地文件
		    File outputFile = new File("downloaded_" + System.currentTimeMillis() + ".jpg");
		    try (InputStream is = connection.getInputStream();
		         FileOutputStream fos = new FileOutputStream(outputFile)) {
		        byte[] buffer = new byte[8192];
		        int length;
		        while ((length = is.read(buffer)) != -1) {
		            fos.write(buffer, 0, length);
		        }
		    }
		    System.out.println("文件下载成功，保存至：" + outputFile.getAbsolutePath());
		} else {
		    // 读取错误信息
		    try (InputStream errorStream = connection.getErrorStream()) {
		        if (errorStream != null) {
		            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
		            String line;
		            while ((line = reader.readLine()) != null) {
		                System.err.println(line);
		            }
		        }
		    }
		    System.out.println("下载失败，HTTP 状态码：" + responseCode);
		}
		connection.disconnect();
	}
}