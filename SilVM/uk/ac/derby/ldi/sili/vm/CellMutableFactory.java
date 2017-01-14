package uk.ac.derby.ldi.sili.vm;


public class CellMutableFactory implements CellFactory {

	public Cell getNewCell() {
		return new CellMutable();
	}

}
