package com.minsait.onesait.platform.reports.converter.base;

import java.io.Serializable;

/**
 * <p>Base converter to transform Bean to Serializable Bean
 * 
 * @author aponcep
 *
 * @param <S>
 * @param <T>
 */
public interface BaseConverter<S, T extends Serializable> {

	T convert(S input);
}
