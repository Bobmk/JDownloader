import java.util.Scanner;
import java.util.Formatter;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class DownloadManager {

    public static void main(String [] args){

        if(args.length<1){
            System.out.println("Usage : DownloadManager <url_string>");
            System.exit(1);
        }
        String urlstr=args[0];

        Downloader download = new Downloader(urlstr);
        download.startDownload();
    }
}

class Downloader {

    private String urlStr;

    public Downloader(String urlData){
        this.urlStr = urlData;
    }

    private static String getSize(long num){
        double doubleVal=(double)num;
        Formatter fmt=new Formatter();
        String ans="";

        if(doubleVal < 1024)
            return new String((int)doubleVal+" bytes");
        
        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.2f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" KB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.1f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" KB";
        } else if(doubleVal < 1024){
            fmt.format("%d",(int)doubleVal);
            return fmt+" KB";
        }

        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.2f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" MB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.1f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" MB";
        } else if(doubleVal < 1024){
            fmt.format("%d",(int)doubleVal);
            return fmt+" MB";
        }

        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.2f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" GB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.1f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" GB";
        } else if(doubleVal < 1024){
            fmt.format("%d",(int)doubleVal);
            return fmt+" GB";
        }

        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.2f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" TB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%.1f",doubleVal);
            else
                fmt.format("%d",(int)doubleVal);
            return fmt+" TB";
        } else if(doubleVal < 1024){
            fmt.format("%d",(int)doubleVal);
            return fmt+" TB";
        }

        return (int)doubleVal+" TB";
    }

    private int percent(long num,long denom){
        float numerator=num;
        float denominator=denom;
        float fraction=numerator/denominator;
        return (int)(fraction*100);
    }

    public void startDownload(){

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Scanner in = new Scanner(System.in);

                try {
                    URL urlObj = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                    // conn.setRequestMethod("GET");
                    // conn.setDoOutput(true);

                    long fileSize = conn.getContentLengthLong();
                    String oldFileName = urlStr.substring( urlStr.lastIndexOf('/')+1, urlStr.length() );
                    String newFileName = null;

                    File file = new File(oldFileName);
                    if(file.exists()){
                        System.out.print("\n"+oldFileName+" already exists\n\nDo you want to overwrite(y/n) : ");
                        char ans=in.next().charAt(0);

                        if(ans != 'y' && ans != 'Y'){
                            System.out.print("\nDo you want to give it a new name(y/n) : ");
                            ans=in.next().charAt(0);

                            if(ans == 'y' || ans == 'Y'){
                                System.out.print("Enter file name : ");
                                newFileName=in.next();
                            }else{
                                System.out.println("\naborted...");
                                System.exit(2);
                            }
                        }
                    }


                    System.out.println("\nDownloading: "+oldFileName +" Size: "+getSize(fileSize));

                    if(newFileName==null)
                        newFileName=oldFileName;


                    // int requestInfo = conn.getResponseCode();
                    BufferedInputStream buffInput = new BufferedInputStream(conn.getInputStream());
                    //writing the data to the file
                    FileOutputStream fileOutput = new FileOutputStream(newFileName);
                    file=new File(newFileName);
                    int len = 0;
                    byte[] buff = new byte[1024];
                    len = buffInput.read(buff);
                    long start,end;

                    start = System.currentTimeMillis();
                    System.out.println();
                    System.out.print( getSize(file.length())+"/"+getSize(fileSize)+" ["+percent(file.length(),fileSize)+"%]\r" );

                    while (len != -1){
                        fileOutput.write(buff,0,len);
                        len = buffInput.read(buff);
                        end=System.currentTimeMillis();
                        if(end-start>=100){
                            start=end;
                            System.out.print( getSize(file.length())+"/"+getSize(fileSize)+"\t["+percent(file.length(),fileSize)+"%]\r" );
                        }
                    }
                    System.out.print( getSize(file.length())+"/"+getSize(fileSize)+"\t[100%]\r" );
                    System.out.println();

                    fileOutput.flush();
                    fileOutput.close();
                    buffInput.close();

                    System.out.println("\nDownload complete...\n");

                } catch (MalformedURLException e) {
                    // e.printStackTrace();
                    System.err.println("\nWrong url\n");

                } catch (IOException e) {
                    // e.printStackTrace();
                    System.err.println("\nan error occured\n");
                }

            }
        };

        Thread subThread = new Thread(r);
        subThread.start();
    }
}
