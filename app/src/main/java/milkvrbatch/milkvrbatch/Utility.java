package milkvrbatch.milkvrbatch;

import android.os.Environment;
import android.os.StrictMode;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	private String mvrlPath;
	private String folderUrl;
	private String webPort;
	private String dlnaPort;
	private String serverIp;

	public Utility(String dPort, String webFolder) throws SecurityException, IOException {
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
				getRootSubFolders();
				getFiles();
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

	public void createMvrls() throws FileNotFoundException, UnsupportedEncodingException {
		for (Map.Entry<String, String> entry : fileMatches.entrySet()) {
			String filePath = entry.getKey();
			String filename = entry.getValue();
			PrintWriter writer = new PrintWriter(mvrlPath + filename + ".mvrl", "UTF-8");
			writer.println(rootDlnaUrl + filePath.replaceAll("/play/", "") + "/" + filename);

			filename = filename.toLowerCase();
			if (filename.matches("(.*)(180x180_3dh)(.*)")) {
				writer.println("180x180_3dh");
			} else if (filename.matches("(.*)(180x180_squished_3dh)(.*)")) {
				writer.println("180x180_squished_3dh");
			} else if (filename.matches("(.*)(180x160_3dv)(.*)")) {
				writer.println("180x160_3dv");
			} else if (filename.matches("(.*)(cylinder_slice_2x25_3dv)(.*)")) {
				writer.println("cylinder_slice_2x25_3dv");
			} else if (filename.matches("(.*)(cylinder_slice_16x9_3dv)(.*)")) {
				writer.println("cylinder_slice_16x9_3dv");
			} else if (filename.matches("(.*)(3dh)(.*)")) {
				writer.println("3dh");
			} else if (filename.matches("(.*)(3dv)(.*)")) {
				writer.println("3dv");
			} else if (filename.matches("(.*)(_2dp)(.*)")) {
				writer.println("_2dp");
			} else if (filename.matches("(.*)(_3dpv)(.*)")) {
				writer.println("_3dpv");
			} else if (filename.matches("(.*)(_3dph)(.*)")) {
				writer.println("_3dph");
			} else if (filename.matches("(.*)(180x180)(.*)")) {
				writer.println("180x180");
			} else if (filename.matches("(.*)(180x101)(.*)")) {
				writer.println("180x101");
			} else if (filename.matches("(.*)(_mono360)(.*)")) {
				writer.println("_mono360");
			} else if (filename.matches("(.*)(180hemispheres)(.*)")) {
				writer.println("180hemispheres");
			} else if (filename.matches("(.*)(_planetarium)(.*)") || filename.matches("(.*)(_fulldome)(.*)")) {
				writer.println("_planetarium");
			} else if (filename.matches("(.*)(_v360)(.*)")) {
				writer.println("_v360");
			} else if (filename.matches("(.*)(_rtxp)(.*)")) {
				writer.println("_rtxp");
			}
			else {writer.println("_2dp");}
			writer.close();
		}
	}


}
