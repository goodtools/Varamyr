package com.khotyn.varamyr;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * User: khotyn
 * Date: 12-1-10
 * Time: PM10:19
 */
public class Varamyr {

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("Please input the epub file path you want to convert!");
            return;
        }

        try {
             parse(args[0]);
        } catch (IOException e) {
            System.out.println("Malformed epub file!!");
        } catch (ParserConfigurationException e) {
            System.out.println("Malformed epub file!!");
        } catch (SAXException e) {
            System.out.println("Malformed epub file!!");
        }
    }

    public static void parse(String filePath) throws IOException, ParserConfigurationException, SAXException {
        if (!filePath.endsWith(".epub")) {
            System.out.println("Please provide an epub file");
            return;
        }

        ZipFile book = new ZipFile(filePath);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filePath.replace(".epub", "-varamyr.epub")));
        Enumeration entries = book.entries();
        List<String> htmls = new ArrayList<String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        byte[] buf = new byte[1024];

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();

            if (zipEntry.getName().endsWith(".opf")) {
                Document document = db.parse(book.getInputStream(zipEntry));
                Element element = document.getDocumentElement();

                NodeList nodeList = element.getElementsByTagName("item");

                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element el = (Element) nodeList.item(i);

                        if (el.getAttribute("media-type").equals("application/xhtml+xml")) {
                            htmls.add(el.getAttribute("href"));
                        }
                    }
                }
            }
        }

        entries = book.entries();

        byte[] css = getCss();

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            String path = zipEntry.getName().substring(zipEntry.getName().lastIndexOf('/') + 1);

            if (htmls.contains(path)) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(book.getInputStream(zipEntry)));
                String line = null;
                out.putNextEntry(new ZipEntry(zipEntry.getName()));

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("<head>")) {
                        int position = line.indexOf("<head>") + "<head>".length();
                        out.write(line.substring(0, position).getBytes());

                        out.write(css);
                        out.write(line.substring(position).getBytes());
                    } else {
                        out.write(line.getBytes());
                    }
                }

                out.closeEntry();
                bufferedReader.close();
            } else {
                InputStream in = book.getInputStream(zipEntry);
                out.putNextEntry(new ZipEntry(zipEntry.getName()));
                int len = 0;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                out.closeEntry();
                in.close();
            }
        }

        out.close();
    }

    public static byte[] getCss() {

        InputStream in  = Varamyr.class.getClassLoader().getResourceAsStream("zw.css");

        try {

            String str = inputStream2String(in);

            return ("\n<style type='text/css'>\n" + str + "\n</style>\n").getBytes();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];

    }


    private static String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line).append("\n");
        }
        return buffer.toString();
    }
}
