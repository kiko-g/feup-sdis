public class ChordMaintainer implements Runnable{
    private ChordNode node;
    private String maintainerType;

    public ChordMaintainer(ChordNode node, String maintainerType) {
        this.node = node;
        this.maintainerType = maintainerType;
    }

	@Override
	public void run() {
        if(this.maintainerType.equals("stabilize")) {
            this.node.stabilize();
        }
        else if(this.maintainerType.equals("fix_fingers")) {
            this.node.fix_fingers();
        }
        else if(this.maintainerType.equals("check_predecessor")) {
            this.node.check_predecessor();
        }
	}
}