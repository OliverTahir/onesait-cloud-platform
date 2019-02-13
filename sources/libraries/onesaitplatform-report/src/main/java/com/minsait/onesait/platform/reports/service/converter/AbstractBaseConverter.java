package com.minsait.onesait.platform.reports.service.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO: Mejorar -> Mucho codigo duplicado
public abstract class AbstractBaseConverter<S, D extends Serializable> implements BaseConverter<S, D> {

	
	public List<D> convert(Iterable<S> iterable) {
		if (iterable == null) {
			return new ArrayList<>();
		}
		
		return StreamSupport.stream(iterable.spliterator(), false)
				.map(input -> convert(input))
                .collect(Collectors.toList());
	}
	
	public List<D> convert(S[] inputs) {
		if (inputs == null) {
			return new ArrayList<>();
		}
		
		return Arrays.stream(inputs)
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
	
	public List<D> convert(S[] inputs, Predicate<S> filterInput) {
		if (inputs == null) {
			return new ArrayList<>();
		}
		
		return Arrays.stream(inputs)
				.filter(filterInput)
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
	
	public List<D> convert(Collection<S> inputs) {
		if (inputs == null) {
			return new ArrayList<>();
		}
		
		return inputs.stream()
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
	
	public List<D> convert(Collection<S> inputs, Predicate<S> filterInput) {
		if (inputs == null) {
			return new ArrayList<>();
		}
		
		return inputs.stream()
				.filter(filterInput)
				.map(input -> convert(input))
				.collect(Collectors.toList());
	}
}
