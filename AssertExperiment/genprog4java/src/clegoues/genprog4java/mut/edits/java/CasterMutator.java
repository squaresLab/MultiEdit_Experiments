package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class CasterMutator extends JavaEditOperation {

	String replaceWithString = null;
	public CasterMutator(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(ASTRewrite rewriter) {
		//ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode toReplace = (ASTNode) thisHole.getCode();
		int rand = Configuration.randomizer.nextInt(thisHole.getSubExps().size());
		ASTNode replaceWith = ASTNode.copySubtree(rewriter.getAST(), thisHole.getSubExps().get(rand));	
		replaceWithString = replaceWith.toString();
		rewriter.replace(toReplace, replaceWith, null);

	}
	public String toString() {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		String retval = "CasterMutator(" + this.getLocation().getId() + ": ";
		retval += "(" + thisHole.getCode() + ") replaced with ";
		retval +=  "(" + replaceWithString + "))";
		return retval;
	}

}






