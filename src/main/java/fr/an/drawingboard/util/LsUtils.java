package fr.an.drawingboard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import lombok.val;

public class LsUtils {

	public static <T> List<T> of(T elt0) {
		return new ArrayList<>(ImmutableList.of(elt0));
	}
	public static <T> List<T> of(T elt0, T elt1) {
		return new ArrayList<>(ImmutableList.of(elt0, elt1));
	}
	@SafeVarargs
	public static <T> List<T> of(T... elts) {
		return new ArrayList<>(Arrays.asList(elts));
	}

	public static <T> List<T> union(List<T> ls1, List<T> ls2) {
		val res = new ArrayList<T>(ls1.size() + ls2.size());
		res.addAll(ls1);
		res.addAll(ls2);
		return res; 
	}

	
	public static <TSrc,TDest> List<TDest> map(Iterable<TSrc> srcs, Function<TSrc,TDest> mapper) {
		val res = new ArrayList<TDest>(); 
		for(val src: srcs) {
			res.add(mapper.apply(src));
		}
		return res;
	}

	public static <TSrc,TDest> List<TDest> map(Collection<TSrc> srcs, Function<TSrc,TDest> mapper) {
		return srcs.stream().map(mapper).collect(Collectors.toList());
	}

}
