package milkvrbatch.milkvrbatch;

import android.os.Environment;
import android.os.StrictMode;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

	private String rootWebUrl;
	private String rootDlnaUrl;
	private Map<String, String> fileMatches;
	private Set<String> folderMatches;
	//private Set<String> jpegs;
	private String mvrlPath;
	private String folderUrl;
	private String webPort;
	private String dlnaPort;
	private boolean useThumbnails;
	private boolean useAudio;
	private boolean useHttp;
	private String serverIp;
	private String thumbnailPath;

	public Utility(String dPort, String webFolder, boolean thumbnails, boolean audio, boolean http) throws SecurityException, IOException {
		if (dPort != null && webFolder != null) {
			System.out.println("Loading properties file...");
			dlnaPort = dPort.trim();
			String pattern = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?";
			Pattern compiledPattern = Pattern.compile(pattern);
			Matcher matcher = compiledPattern.matcher(webFolder.trim());
			if (matcher.find()) {
				String address = matcher.group();
				String[] parts = address.split(":");
				serverIp = parts[0];
				webPort = parts[1];
				parts = webFolder.split(address);
				folderUrl = "http://" + address + parts[1];
			}

			useThumbnails = thumbnails;
			useAudio = audio;
			useHttp = http;
			rootWebUrl = "http://" + serverIp + ":" + webPort;
			rootDlnaUrl = "http://" + serverIp + ":" + dlnaPort + "/get/";
			fileMatches = new HashMap<String, String>();
			folderMatches = new HashSet<String>();
		}
	}

	public String run() {
		try {
			int SDK_INT = android.os.Build.VERSION.SDK_INT;
			if (SDK_INT > 8) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
				mvrlPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MilkVR" + File.separator;
				File mvrlDir = new File(mvrlPath);
				if (!mvrlDir.exists()) {
					mvrlDir.mkdirs();
				} else if (mvrlDir.exists() && !mvrlDir.isDirectory()) {
					return "File exists at MilkVR";
				}
				if (!mvrlDir.exists()) {
					return "MilkVR folder could not be created";
				}

				if(useThumbnails && !useHttp){
					thumbnailPath = mvrlPath + "thumbnails" + File.separator;
					File thumbnailDir = new File(thumbnailPath);
				if (!thumbnailDir.exists()) {
					thumbnailDir.mkdirs();
				} else if (thumbnailDir.exists() && !thumbnailDir.isDirectory()) {
					return "File exists at thumbnail directory";
				}
				if (!thumbnailDir.exists()) {
					return "thumbnail folder could not be created";
				}}

				if (folderUrl.matches("(.*)(/play/)(.*)")) {
					getFile();
				}
				else{
					getRootSubFolders();
					getFiles();}

				createMvrls();
			}
		} catch (Exception e) {
			//return e.toString();
			return ("STUFF BROKE");
		}
		return "SUCCESS. Check your MilkVR folder";
	}

	public void getFiles() throws IOException {
		for (String folder : folderMatches) {
			Connection connection = Jsoup.connect(rootWebUrl + folder).timeout(600000);
			Document doc = connection.get();
			connection = null;
			Element container = doc.body().getElementById("Container");
			Element folders = container.getElementById("Media");
			if (folders != null) {
				Elements links = folders.select("li");
				for (Element link : links) {
					Element first = link.child(0);
					if (first.attr("title").matches("^.*?(.webm|.mkv|.flv|.flv|.vob|.ogv|.ogg|.drc|.gif|.gifv|.mng|.avi|.mov|.qt|.wmv|.yuv|.rm|.rmvb|.asf|.mp4|.m4p|.m4v|.mpg|.mp2|.mpeg|.mpe|.mpv|.mpg|.mpeg|.m2v|.m4v|.svi|.3gp|.3g2|.mxf|.roq|.nsv|.flv|.f4v|.f4p|.f4a|.f4b)$")) {
						fileMatches.put(first.attr("href"), first.attr("title"));
						}
				}
			}
			doc = null;
		}
	}

	public void getFile() throws IOException {
			Connection connection = Jsoup.connect(folderUrl).timeout(600000);
			Document doc = connection.get();
			String title = doc.title();

			fileMatches.put(folderUrl.replace(rootWebUrl, ""), title);
			doc = null;
	}

	public void getRootSubFolders() throws IOException {
		folderMatches.add(folderUrl.replace(rootWebUrl, ""));
		getSubFolders(folderUrl);
	}

	public void getSubFolders(String url) throws IOException {
		Connection connection = Jsoup.connect(url).timeout(600000);
		Document doc = connection.get();
		connection = null;
		Element container = doc.body().getElementById("Container");
		Element folders = container.getElementById("FoldersContainer").getElementById("Folders");
		Elements links = folders.select("a[href]");
		for (Element link : links) {
			String href = link.attr("href");
			if (!href.equals("javascript:history.back()")) {
				folderMatches.add(href);
				getSubFolders(rootWebUrl + href);
			}
		}
		doc = null;
	}

	public void createMvrls() throws IOException {
		URL thumbnailURL;
		for (Map.Entry<String, String> entry : fileMatches.entrySet()) {

			String filePath = entry.getKey();
			String filename = entry.getValue();
			String id = filePath.replaceAll("/play/", "");
			BufferedWriter writer = new BufferedWriter(new FileWriter(mvrlPath + filename + ".mvrl", false));
			writer.write(rootDlnaUrl + id + "/" + filename);
			writer.newLine();
			filename = filename.toLowerCase();
			if (filename.matches("(.*)(180x180_3dh)(.*)")) {
				writer.write("180x180_3dh");
			} else if (filename.matches("(.*)(180x180_squished_3dh)(.*)")) {
				writer.write("180x180_squished_3dh");
			} else if (filename.matches("(.*)(180x160_3dv)(.*)")) {
				writer.write("180x160_3dv");
			} else if (filename.matches("(.*)(cylinder_slice_2x25_3dv)(.*)")) {
				writer.write("cylinder_slice_2x25_3dv");
			} else if (filename.matches("(.*)(cylinder_slice_16x9_3dv)(.*)")) {
				writer.write("cylinder_slice_16x9_3dv");
			} else if (filename.matches("(.*)(3dh)(.*)")) {
				writer.write("3dh");
			} else if (filename.matches("(.*)(3dv)(.*)")) {
				writer.write("3dv");
			} else if (filename.matches("(.*)(_2dp)(.*)")) {
				writer.write("_2dp");
			} else if (filename.matches("(.*)(_3dpv)(.*)") ||
					(filename.matches("(.*)(3d)(.*)") &&
							(filename.matches("(.*)(h-ou)(.*)") ||  filename.matches("(.*)(half-ou)(.*)")))) {
				writer.write("_3dpv");
			} else if (filename.matches("(.*)(_3dph)(.*)") ||
					(filename.matches("(.*)(3d)(.*)") &&
							(filename.matches("(.*)(h-sbs)(.*)") ||  filename.matches("(.*)(half-sbs)(.*)")))) {
				writer.write("_3dph");
			} else if (filename.matches("(.*)(180x180)(.*)")) {
				writer.write("180x180");
			} else if (filename.matches("(.*)(180x101)(.*)")) {
				writer.write("180x101");
			} else if (filename.matches("(.*)(_mono360)(.*)")) {
				writer.write("_mono360");
			} else if (filename.matches("(.*)(180hemispheres)(.*)")) {
				writer.write("180hemispheres");
			} else if (filename.matches("(.*)(_planetarium)(.*)") || filename.matches("(.*)(_fulldome)(.*)")) {
				writer.write("_planetarium");
			} else if (filename.matches("(.*)(_v360)(.*)")) {
				writer.write("_v360");
			} else if (filename.matches("(.*)(_rtxp)(.*)")) {
				writer.write("_rtxp");
			}
			else {writer.write("_2dp");}

			writer.newLine();

			if (useAudio) {
				if (filename.matches("(.*)(5.1)(.*)")) {
					writer.write("_5.1");
				}else if (filename.matches("(.*)(quadraphonic)(.*)")) {
					writer.write("quadraphonic");
				}else if (filename.matches("(.*)(binaural)(.*)")) {
					writer.write("_binaural");
				}}

			writer.newLine();

			if (useThumbnails) {
				if(useHttp){
					writer.write(rootDlnaUrl + id + "/" + "thumbnail0000.jpg");
					Connection connection = Jsoup.connect(rootDlnaUrl + id + "/" + "thumbnail0000.jpg").timeout(600000);}
				else {
					String thumbnail = thumbnailPath + filename + ".jpg";
					URL url = new URL(rootWebUrl + filePath.replaceAll("play", "thumb"));
					InputStream is = url.openStream();
					OutputStream os = new FileOutputStream(thumbnail);
					byte[] b = new byte[24576];
					int length;
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
					}
					is.close();
					os.close();
					writer.write(thumbnail);
				}
			}

			writer.close();
		}
	}


}
