package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.an.drawingboard.model.shape.Shape;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.paint.Color;
import lombok.val;

public class TraceGesture {

	public Color color;
	public int lineWidth;
	
	public List<TracePath> pathes = new ArrayList<>();
	
	public Shape recognizedShape;
	
	public void removeLastPath() {
		if (! pathes.isEmpty()) {
			pathes.remove(pathes.size() - 1);
		}
	}

	public boolean isEmpty() {
		return pathes.isEmpty();
	}
	
	public TracePath getLast() {
		return (pathes.isEmpty())? null : pathes.get(pathes.size() - 1);
	}
	
	public TracePath appendNewPath() {
		val res = new TracePath();
		pathes.add(res);
		return res;
	}
	

	public List<Double> pathDistLengths() {
		List<Double> res = new ArrayList<>(pathes.size());
		for(val path : pathes) {
			res.add(path.pathDistLength());
		}
		return res;
	}

	public static class TracePathWithElement {
		public TracePath path;
		public TracePathElement pathElement;
	}

	public Iterator<TracePathWithElement> iteratorPathWithElement() {
		return new TracePathWithElementIterator(pathes.iterator());
	}

	private static class TracePathWithElementIterator implements Iterator<TracePathWithElement> {
		final TracePathWithElement curr = new TracePathWithElement();
		final Iterator<TracePath> pathIter;
		Iterator<TracePathElement> pathElementIter;

		private TracePathWithElementIterator(Iterator<TracePath> pathIter) {
			this.pathIter = pathIter;
		}

		@Override
		public boolean hasNext() {
			if (pathElementIter != null && pathElementIter.hasNext()) {
				return true;
			}
			return pathIter.hasNext();
		}

		@Override
		public TracePathWithElement next() {
			if (curr.path == null) {
				DrawingValidationUtils.checkTrue(pathIter.hasNext(), "hasNext");
				curr.path = pathIter.next();
				this.pathElementIter = curr.path.pathElements.iterator(); 
			}
			curr.pathElement = pathElementIter.next();
			if (! pathElementIter.hasNext()) {
				pathElementIter = null;
			}
			return curr;
		}
		
	}

}
