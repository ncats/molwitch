package gov.nih.ncats.witch;

import java.util.List;

public interface PathVisitor {

	void visit(List<Bond> path);
}
