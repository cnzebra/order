package com.saituo.order.service.variable;

import java.io.File;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.saituo.order.commons.page.Page;
import com.saituo.order.commons.page.PageRequest;
import com.saituo.order.entity.order.Product;

@Service
public class ProductInfoSearcher {

	private static final int MAX_COUNT = 100;

	@Value("${config.search.indexDir}")
	private String indexDir;

	@Autowired
	private Analyzer luceneAnalyzer;

	public Page<Product> searchForPublic(PageRequest pageRequest, String queryStr) {

		int pageSize = pageRequest.getPageSize();
		int currentPage = pageRequest.getPageNumber();

		// 待查找字符串对应的字段
		String[] fields = {"product_num", "product_name", "brand_name"};
		Occur[] occ = {Occur.SHOULD, Occur.SHOULD, Occur.SHOULD};

		List<Product> productList = Lists.newArrayList();
		try {
			Query query = MultiFieldQueryParser.parse(Version.LUCENE_40, queryStr, fields, occ, luceneAnalyzer);

			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
			IndexSearcher searcher = new IndexSearcher(reader);
			// 之返回前100条记录
			TopDocs topDocs = searcher.search(query, MAX_COUNT);
			// 搜索结果总数量
			int totalCount = Math.min(topDocs.totalHits, MAX_COUNT);
			// 搜索返回的结果集合
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;

			// 查询起始记录位置
			int begin = pageSize * (currentPage - 1);
			// 查询终止记录位置
			int end = Math.min(begin + pageSize, scoreDocs.length);

			// 进行分页查询
			for (int i = begin; i < end; i++) {
				int docID = scoreDocs[i].doc;
				Document doc = searcher.doc(docID);
				Product product = new Product();
				product.setProductId(Integer.valueOf(doc.get("product_id")));
				product.setProductName(doc.get("product_name"));
				product.setProductNum(doc.get("product_num"));
				product.setSpecValue(doc.get("spec_value"));
				product.setUnitValue(doc.get("unit_value"));
				product.setBrandId(doc.get("brand_name"));
				product.setCatalogFee(Double.valueOf(doc.get("catalog_fee")));
				product.setWeightDiscount(Double.valueOf(doc.get("weight_discount")));
				product.setBuyDiscount(Double.valueOf(doc.get("buy_discount")));
				productList.add(product);
			}
			return new Page<Product>(pageRequest, productList, totalCount);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Page<Product>(pageRequest, productList, 0);
	}
}
