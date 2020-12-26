package fr.an.drawingboard.model.trace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.an.drawingboard.geom2d.Pt2D;
import fr.an.drawingboard.geom2d.utils.DistinctPt2DListBuilder;
import fr.an.drawingboard.util.DrawingValidationUtils;
import javafx.scene.paint.Color;
import lombok.val;

public class TraceGesture {

	public Color color;
	public int lineWidth;
	
	private List<TracePath> pathes = new ArrayList<>();
	
	// ------------------------------------------------------------------------
	
	public void removeLastPath() {
		if (! pathes.isEmpty()) {
			pathes.remove(pathes.size() - 1);
		}
	}

	public int size() {
		return pathes.size();
	}

	public boolean isEmpty() {
		return pathes.isEmpty();
	}
	
	public TracePath get(int i) {
		return pathes.get(i);
	}
	
	public TracePath getLast() {
		return (pathes.isEmpty())? null : pathes.get(pathes.size() - 1);
	}
	
	public void addPath(TracePath path) {
		this.pathes.add(path);
	}


	public List<Double> pathDistLengths() {
		List<Double> res = new ArrayList<>(pathes.size());
		for(val path : pathes()) {
			res.add(path.pathDistLength());
		}
		return res;
	}


	public Iterable<TracePath> pathes() {
		return pathes;
	}
	
	
	
	public static class TracePathElementEntry {
		public TracePath path;
		public TracePathElement pathElement;
	}

	public Iterator<TracePathElementEntry> iteratorPathElementEntries() {
		return new TracePathElementEntryIterator(pathes.iterator());
	}

	private static class TracePathElementEntryIterator implements Iterator<TracePathElementEntry> {
		final TracePathElementEntry curr = new TracePathElementEntry();
		final Iterator<TracePath> pathIter;
		Iterator<TracePathElement> pathElementIter;

		private TracePathElementEntryIterator(Iterator<TracePath> pathIter) {
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
		public TracePathElementEntry next() {
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
