package lexical;
import java.io.BufferedReader;
import java.io.IOException;

public class fileProcess {
    private String code;
    private BufferedReader reader;

    public fileProcess(BufferedReader reader) throws IOException {
        this.reader=reader;
        code = file2code(this.getReader());
    }

    private String file2code(BufferedReader reader) throws IOException {
        StringBuffer str = new StringBuffer();
        String s = null;
        while ((s = this.getReader().readLine()) != null){
            str.append(s).append("\n");
        }
        return str.toString();
    }

    public BufferedReader getReader() {
        return reader;
    }

    public String getCode() {
        return code;
    }
}
