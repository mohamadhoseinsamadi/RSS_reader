import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("welcome to rss reader");
        Scanner input=new Scanner(System.in);
        PrintStream printer;

        FileWriter writer = new FileWriter("rssreader.txt",true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        FileWriter writerlinks = new FileWriter("links.txt",true);
        BufferedWriter bufferedWriterlinks = new BufferedWriter(writerlinks);

        boolean bool=true;

        ArrayList<String> URLs = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();

        FileReader fileReader = new FileReader("rssreader.txt");
        BufferedReader reader = new BufferedReader(fileReader);
        FileReader linkreader = new FileReader("links.txt");
        BufferedReader readerlinks = new BufferedReader(linkreader);

        String line;
        for(int j=0; ((line = reader.readLine()) != null);j++){
            URLs.add(line);
        }
        reader.close();
        String line2;
        for(int j=0; ((line2 = readerlinks.readLine()) != null);j++){
            links.add(line2);
        }
        readerlinks.close();

        while (bool){


            System.out.println("type a valid number for your desired action :");
            System.out.println("[1] Show Updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URL");
            System.out.println("[4] Exit");
            String action=input.next();//به صورت رشته گرفتم تا اگه کاربر اشتباهی رشته وارد کرد بهش پیغام خطا بدم(خودم صد بار در حین اجرا این اشتباه رو کرده بودم/: )
            input.nextLine();

            switch (action){
                case "1":{
                    System.out.println("[0] All website");
                    for(int i=0;i<URLs.size();i++)
                        System.out.println("["+(i+1)+"] "+URLs.get(i));
                    System.out.println("Enter -1 to return.");
                    int i=input.nextInt();
                    input.nextLine();
                    if(i==-1)
                        break;
                    else if(i==0){
                        for(int k=0;k<links.size();k++){
                            System.out.println(URLs.get(k));
                            retrieveRssContent(extractRssUrl(links.get(k)));
                        }
                    }
                    else {
                        System.out.println(URLs.get(i-1));
                        retrieveRssContent(extractRssUrl(links.get(i-1)));
                    }
                }
                break;
                case "2":{
                    System.out.println("please enter website URL to add:");
                    String link=input.nextLine();
                    String pagesorce=fetchPageSource(link);
                    while (pagesorce.equals("")) {
                        System.out.println(",please enter website URL again to add(Enter -1 to return");
                        link = input.nextLine();
                        if(link.equals("-1"))
                            break;
                        else
                            pagesorce=fetchPageSource(link);
                    }
                    if(link.equals("-1"))
                        break;
                    String titleOfPage=extractPageTitle(pagesorce);
                    if(!URLs.contains(titleOfPage)){
                        URLs.add(titleOfPage);
                        links.add(link);
                        System.out.println("Added "+ link+ " successfully");
                    }
                    else
                        System.out.println(link+" already exist!");
                }
                break;
                case "3":{
                    System.out.println("please enter website URL to remove :");
                    String link=input.nextLine();
                    String pagesorce=fetchPageSource(link);
                    while (pagesorce.equals("")) {
                        System.out.println(",please enter website URL again to remove(Enter -1 to return)");
                        link = input.nextLine();
                        if(link.equals("-1"))
                            break;
                        else
                            pagesorce=fetchPageSource(link);
                    }
                    if(link.equals("-1"))
                        break;
                    String titleOfPage=extractPageTitle(pagesorce);
                    if(URLs.contains(titleOfPage)){
                        URLs.remove(titleOfPage);
                        links.remove(link);
                        System.out.println("it removed succesfully");
                    }
                    else
                        System.out.println("Could not find "+link);

                }
                break;
                case "4":{
                    bool=false;
                }
                break;
                default:
                    System.out.println("invalid command");

            }

        }
        for(int i=0;i<URLs.size();i++)
            bufferedWriter.write(URLs.get(i)+"\n");
        for(int i=0;i<links.size();i++)
            bufferedWriterlinks.write(links.get(i)+"\n");
        bufferedWriterlinks.close();
        bufferedWriter.close();
        System.out.println("good bye");



    }
    //متد زیر برای به دست آوردن عنوان یک وبسایت از روی سورس html اش است
    public static String extractPageTitle(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e){
            return "Error: no title tag found in page source!";
        }
    }

    //این متد برای استخراج محتویات RSS از روی نشانی آن است:
    public static void retrieveRssContent(String rssUrl){
        try{
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < 5; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    //با این متد می توانید نشانی RSS یک وبسایت را با داشتن url آن استخراج کنید:
    public static String extractRssUrl(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    //از این متد برای به دست آوردن سورس html یک وبسایت از روی url آن استفاده کنید:
    public static String fetchPageSource(String urlString) {
        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
            return toString(urlConnection.getInputStream());
        }
        catch (Exception e){
            System.out.print("Something went wrong(internet error or wrong URL)");
            return "";
        }
    }

    //در متد بالا از متد کمکی زیر هم استفاده شده است:
    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }

}