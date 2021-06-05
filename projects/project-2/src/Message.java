import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class Message implements java.io.Serializable {
    private byte[] body;
    private byte[] h;
    private String[] header;
    private byte[] message;
    private static final long serialVersionUID = 4066270093854086490L;

    public Message(byte[] message) {
        this.body = null;
        this.h = null;
        this.message = message;
        splitHeaderAndBody();

        this.header = new String(this.h).split(" ");
    }


    public void splitHeaderAndBody() {
        int i;
        for(i = 0; i < this.message.length; i++) {
            if(this.message[i] == 0xD && this.message[i + 1] == 0xA && this.message[i + 2] == 0xD && this.message[i + 3] == 0xA) {
                break;
            }
        }

        this.h = Arrays.copyOfRange(this.message, 0, i);
        this.body = Arrays.copyOfRange(this.message, i + 4, message.length); // i+4 because between i and i+4 are \r\n\r\n
    }


    public byte[] getBody() {
        return this.body;
    }


    public String[] getHeader() {
        return this.header;
    }


    @Override
    public String toString() {
        String ret = null;

        for(int i = 0; i < this.header.length; i++) {
            ret += this.header[i] + " ";
        }

        return ret;
    }
}
