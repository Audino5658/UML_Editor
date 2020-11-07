package oo_homework;

public class UML_lines {
	// record the points of the line
	public int linePortX1;
	public int linePortY1;
	public int linePortX2;
	public int linePortY2;
	public boolean isDraggingLine = false;
	public boolean genLine = false;
	public int lineObjNum1 = -1;
	public int lineObjNum2 = -1;
	public int portPos = -1;
	public int lineType = -1;
	public final int ASSOC_LINE = 1;
	public final int GENER_LINE = 2;
	public final int COMP_LINE  = 3;
}
