package uk.ac.derby.ldi.sili.vm;

public class CellMutableFactory implements CellFactory {

	private static final long serialVersionUID = 1L;

	public Cell getNewCell() {
		return new CellMutable();
	}
}
