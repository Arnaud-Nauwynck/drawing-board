package fr.an.drawingboard.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LsUtils {

	public static <TSrc,TDest> List<TDest> map(Collection<TSrc> srcs, Function<TSrc,TDest> mapper) {
		return srcs.stream().map(mapper).collect(Collectors.toList());
	}
}
