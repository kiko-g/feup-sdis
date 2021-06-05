import java.util.Objects;

public class Chunk {
    int id, repDeg;
    String filePath;
    int desRepDeg;
    double size;
    String fileId;
    Chunk(int id,int repDeg, int desRepDeg, String filePath)
    {
        this.id=id;
        this.repDeg=repDeg;
        this.filePath=filePath;
        this.desRepDeg=desRepDeg;
    }
    Chunk(String fileId, int id, double size )
    {
        this.id=id;
        this.repDeg=repDeg;
        this.size=size;
        this.fileId=fileId;
    }
    Chunk(String fileId, int id)
    {
        this.id=id;
        this.fileId=fileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;
        Chunk chunk = (Chunk) o;
        return id == chunk.id && Objects.equals(fileId, chunk.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileId);
    }
}
