package WebCrawler;

import java.util.ArrayList;

public class Stemmer implements Runnable {
	 // array of file paths
    ArrayList<String> Docs;
    int start, end;

    public Stemmer(ArrayList<String> Docs, int start, int end) {
        this.Docs = Docs;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        for (int i = this.start; i < this.end; i++) {
            PorterStemmer st = new PorterStemmer();
            // take file dir and stem it
            String temp = st.stem(Constants.filesDir + this.Docs.get(i));
            // output stemmed result
            new OutputFile(Constants.stemmedDir + this.Docs.get(i), temp);
        }
    }

    public static void main(String[] args) {
        ArrayList<String> files = new ArrayList<String>();
        files.add("Files/in3.txt");
        Thread t1 = new Thread(new Stemmer(files, 0, 1));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }
}
