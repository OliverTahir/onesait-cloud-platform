package com.minsait.onesait.platform.controlpanel.converter.reports;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO: Mejorar -> Mucho codigo duplicado
public abstract class AbstractBaseConverter<S, D extends Serializable> implements BaseConverter<S, D> {

	
	public List<D> convert(Iterable<S> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false)
				.map(input -> convert(input))
                .collect(Collectors.toList());
	}
	
	public List<D> convert(S[] array) {
		return Arrays.stream(array)
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
	
	public List<D> convert(S[] array, Predicate<S> filterInput) {
		return Arrays.stream(array)
				.filter(filterInput)
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
	
	public List<D> convert(Collection<S> inputs) {
		return inputs.stream()
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
	
	public List<D> convert(Collection<S> inputs, Predicate<S> filterInput) {
		return inputs.stream()
				.filter(filterInput)
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
}
