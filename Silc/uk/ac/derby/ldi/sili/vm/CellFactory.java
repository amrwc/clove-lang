package uk.ac.derby.ldi.sili.vm;

import java.io.Serializable;

public interface CellFactory extends Serializable {
	public Cell getNewCell();
}
