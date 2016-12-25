import java.util.Scanner;
import java.util.Formatter;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

class JDownloader {

    public static void help(){
        System.out.println(
            "Syntax: JDownloader [options] <url_string>\n\n"+
            "Options\n"+
            "-------\n"+
            "    -c : continue download from where you left\n"+
            "    -h : show this help message\n"
            );    
    }

    public static void main(String [] args){

        if(args.length<1){
            help();
            System.exit(1);
        }
        String urlstr = null;
        boolean contnu = false;

        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("-")){
                if(args[i].equals("-c"))
                    contnu = true;
                else if(args[i].equals("-h")){
                    help();
                }
                else{
                    System.err.println("wrong option");
                    System.exit(5);
                }
            }else{
                urlstr = args[i];
            }
        }

        if(urlstr == null){
            System.exit(6);
        }

        Downloader download = new Downloader(urlstr);
        if(contnu)
            download.continueDownload();
        else
            download.startDownload();
    }
}

class Downloader {

    private String urlStr;
    private URL urlObj;
    private HttpURLConnection conn;

    public Downloader(String urlData){
        urlStr = urlData;
       
        try {
            // usring the url to create a http connection
            urlObj = new URL(urlStr);
            conn = (HttpURLConnection) urlObj.openConnection();
            urlStr = URLDecoder.decode(urlStr,"UTF-8");
        } catch(UnsupportedEncodingException e){
            System.err.println("error in url");
            System.exit(2);
        } catch (MalformedURLException e){
            System.err.println("wrong url");
            System.exit(2);
        } catch (Exception e){
            System.err.println("connection error");
            System.exit(3);
        }
    }

    private String getSize(long num){
        double doubleVal=(double)num;
        Formatter fmt=new Formatter();
        String ans="";

        if(doubleVal < 1024)
            return new String((int)doubleVal+" bytes");
        
        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.2f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" KB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.1f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" KB";
        } else if(doubleVal < 1024){
            fmt.format("%4d",(int)doubleVal);
            return fmt+" KB";
        }

        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.2f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" MB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.1f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" MB";
        } else if(doubleVal < 1024){
            fmt.format("%4d",(int)doubleVal);
            return fmt+" MB";
        }

        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.2f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" GB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.1f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" GB";
        } else if(doubleVal < 1024){
            fmt.format("%4d",(int)doubleVal);
            return fmt+" GB";
        }

        doubleVal/=1024;

        if(doubleVal < 10){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.2f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" TB";
        } else if(doubleVal < 100){
            if(doubleVal != (int)doubleVal)
                fmt.format("%4.1f",doubleVal);
            else
                fmt.format("%4d",(int)doubleVal);
            return fmt+" TB";
        } else if(doubleVal < 1024){
            fmt.format("4%d",(int)doubleVal);
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

    private String getFileName(String urlStr){
        String name = urlStr.substring( urlStr.lastIndexOf('/')+1, urlStr.length() ).replace(' ','_');
        return name;
    }

    public void startDownload(){

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Scanner in = new Scanner(System.in);
                long fileSize = conn.getContentLengthLong();
                String oldFileName = getFileName(urlStr);
                String newFileName = null;

                // conn.setRequestMethod("GET");
                // conn.setDoOutput(true);
             
                File file = new File(oldFileName);
                if(file.exists()){
                    System.out.print("\n"+oldFileName+" already exists\nDo you want to overwrite(y/n) : ");
                    char ans=in.next().charAt(0);

                    if(ans != 'y' && ans != 'Y'){
                        System.out.print("Do you want to give it a new name(y/n) : ");
                        ans=in.next().charAt(0);

                        if(ans == 'y' || ans == 'Y'){
                            System.out.print("Enter file name : ");
                            newFileName=in.next();
                        }else{
                            System.err.println("aborted");
                            System.exit(4);
                        }
                    }
                }

                if(newFileName == null)
                    newFileName = oldFileName;

                System.out.println("downloading: "+newFileName +" ("+getSize(fileSize)+")");

                // int requestInfo = conn.getResponseCode();

                try {
                    // reading from the connection
                    BufferedInputStream buffInput = new BufferedInputStream(conn.getInputStream());
                    // writing the data to the file
                    FileOutputStream fileOutput = new FileOutputStream(newFileName);

                    file = new File(newFileName);
                    int len = 0;
                    byte[] buff = new byte[1024];
                    len = buffInput.read(buff);
                    long start,end;

                    start = System.currentTimeMillis();
                    System.out.println();
                    System.out.print( getSize(file.length())+"/"+getSize(fileSize)+"  ["+percent(file.length(),fileSize)+"%]" );

                    while (len != -1){
                        fileOutput.write(buff,0,len);
                        len = buffInput.read(buff);
                        end=System.currentTimeMillis();
                        if(end-start >= 100){
                            start=end;
                            System.out.print( "\r"+getSize(file.length())+"/"+getSize(fileSize)+"  ["+percent(file.length(),fileSize)+"%]" );
                        }
                    }
                    System.out.print( "\r"+getSize(file.length())+"/"+getSize(fileSize)+"  ["+percent(file.length(),fileSize)+"%]\n" );

                    fileOutput.flush();
                    fileOutput.close();
                    buffInput.close();

                    System.out.println("download complete...");

                } catch (IOException e) {
                    System.err.println("error occured");
                }

            }
        };

        Thread subThread = new Thread(r);
        subThread.start();
    }

    public void continueDownload(){

        Runnable r = new Runnable() {
            @Override
            public void run(){
                long fileSize = conn.getContentLengthLong();
                String fileName = getFileName(urlStr);

                File file = new File(fileName);

                try {
                    // reading from the connection
                    BufferedInputStream buffInput = new BufferedInputStream(conn.getInputStream());
                    long bytesToDownload;
                    int len = 0;
                    long downloaded = 0;
                    byte[] buff = new byte[1024];

                    if(file.exists()){
                        long bytesToSkip = buffInput.skip(file.length());
                        RandomAccessFile fileOutput = new RandomAccessFile(file,"rw");
                        fileOutput.seek(bytesToSkip);
                        bytesToDownload = fileSize - bytesToSkip;
                        System.out.println("downloading: "+getSize(bytesToDownload)+" of "+getSize(fileSize));

                        len = buffInput.read(buff);
                        downloaded += len;
                        long start,end;

                        start = System.currentTimeMillis();
                        System.out.println();
                        System.out.print( getSize(downloaded)+"/"+getSize(bytesToDownload)+"  ["+percent(0,bytesToDownload)+"%]" );

                        while (len != -1){
                            fileOutput.write(buff,0,len);
                            len = buffInput.read(buff);
                            downloaded += len;
                            end=System.currentTimeMillis();
                            if(end-start >= 100){
                                start=end;
                                System.out.print( "\r"+getSize(downloaded)+"/"+getSize(bytesToDownload)+"  ["+percent(downloaded,bytesToDownload)+"%]" );
                            }
                        }
                        downloaded++;
                        System.out.print( "\r"+getSize(downloaded)+"/"+getSize(bytesToDownload)+"  ["+percent(downloaded,bytesToDownload)+"%]\n" );

                        fileOutput.close();
                        buffInput.close();

                        System.out.println("download complete...");
                    }else{
                        FileOutputStream fileOutput = new FileOutputStream(fileName);
                        bytesToDownload = fileSize;
                        System.out.println("file not found\ndownloading entire file");
                        System.out.println("downloading: "+getSize(bytesToDownload)+" of "+getSize(fileSize));

                        len = buffInput.read(buff);
                        downloaded += len;
                        long start,end;

                        start = System.currentTimeMillis();
                        System.out.println();
                        System.out.print( getSize(downloaded)+"/"+getSize(bytesToDownload)+" ["+percent(downloaded,bytesToDownload)+"%]" );

                        while (len != -1){
                            fileOutput.write(buff,0,len);
                            len = buffInput.read(buff);
                            downloaded += len;
                            end=System.currentTimeMillis();
                            if(end-start >= 100){
                                start=end;
                                System.out.print( "\r"+getSize(downloaded)+"/"+getSize(bytesToDownload)+"\t["+percent(downloaded,bytesToDownload)+"%]" );
                            }
                        }
                        downloaded++;
                        System.out.print( "\r"+getSize(downloaded)+"/"+getSize(bytesToDownload)+"\t["+percent(downloaded,bytesToDownload)+"%]\n" );

                        fileOutput.flush();
                        fileOutput.close();
                        buffInput.close();

                        System.out.println("download complete...");
                    }
                } catch(FileNotFoundException e){
                    System.out.println("file not found");
                } catch (IOException e){
                    System.err.println("error occured");
                }
            }
        };

        Thread subThread = new Thread(r);
        subThread.start();
    }

}
