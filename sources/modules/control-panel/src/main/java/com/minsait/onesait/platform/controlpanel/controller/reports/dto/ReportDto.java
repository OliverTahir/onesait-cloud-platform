package com.minsait.onesait.platform.controlpanel.controller.reports.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReportDto implements Serializable {

	private static final long serialVersionUID = 8529188708539218088L;

	private Long id;
	
	private String name;
	
	private String description;
	
	private String owner;
	
	// <td th:text="${#dates.format(sprint.releaseDate, 'dd-MMM-yyyy')}"></td>
	private Date created;
	
	private Boolean isPublic;
		
	private MultipartFile file;
}
