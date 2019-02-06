package com.minsait.onesait.platform.controlpanel.converter.reports;

import java.io.Serializable;

/**
 * <p>Base converter to transform Bean to Serializable Bean
 * 
 * @author aponcep
 *
 * @param <S>
 * @param <T>
 */
public interface BaseConverter<S, T> {

	T convert(S input);
}
