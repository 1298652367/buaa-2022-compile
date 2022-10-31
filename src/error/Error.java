package error;
public class Error {
    private int n;
    private String type;

    public Error(int n,String type){
        this.n = n;
        this.type = type;
    }

    @Override
    public String toString() {
        return n + " " + type;
    }

    public int getN(){
        return this.n;
    }
}
