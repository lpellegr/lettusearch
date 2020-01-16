package com.redislabs.lettusearch.index;

import java.util.List;

import com.redislabs.lettusearch.search.field.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Builder
public @Getter @Setter class IndexInfo {
	private String indexName;
	private Long numDocs;
	private List<Object> indexOptions;
	private List<Field> fields;
	private String maxDocId;
	private Long numTerms;
	private Long numRecords;
	private Double invertedSizeMb;
	private Long totalInvertedIndexBlocks;
	private Double offsetVectorsSizeMb;
	private Double docTableSizeMb;
	private Double sortableValuesSizeMb;
	private Double keyTableSizeMb;
	private Double recordsPerDocAvg;
	private Double bytesPerRecordAvg;
	private Double offsetsPerTermAvg;
	private Double offsetBitsPerRecordAvg;
	private List<Object> gcStats;
	private List<Object> cursorStats;
}