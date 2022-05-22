package com.ram.controller;

import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.FindIterable;
import com.ram.Database.MongoDatabase;
import com.ram.Results.PageRanker;
import com.ram.Results.PageResult;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ram.model.Page;
import java.util.*;

@Controller
public class PageController
{
	@RequestMapping("/")
	public String index()
	{
		return "index";
	}

	private final int MAX_PER_PAGE = 20;
	ArrayList<ArrayList<PageResult>> PaginationList = new ArrayList<>();
	MongoDatabase Database;

	public ArrayList<PageResult> retrievePagesFromQuery(ArrayList<String> words) {
		ArrayList<PageResult> results = new ArrayList<>();
		for (String word : words)
		{
			double IDF = Database.getWordIDF(word);
			List<Document> DocumentsPerWord = Database.getDocumentsOfWord(word);
			for (Document document : DocumentsPerWord)
			{
				String URL = document.getString("URL");
				FindIterable<Document> PageResultDocument = Database.getSearchDocument(URL);
				double TF = (double) document.get("TF");
				int Position = (int) document.get("Position");
				double TFIDF = calculateTFIDF(IDF, TF);

				//Creating new page result and setting parameters
				PageResult pageresult = new PageResult();
				pageresult.setTFIDF(TFIDF);
				pageresult.setPositionScore(Position);
				//always iterates once
				for (Document resultDoc : PageResultDocument)
				{
					pageresult.setSearchDocument(resultDoc);
				}
				results.add(pageresult);
			}
		}
		return results;
	}

	public double calculateTFIDF(double IDF, double TF)
	{
		return (IDF * TF);
	}

	public void DividePaginations(ArrayList<PageResult> PageResults)
	{
		if(PageResults.size() < MAX_PER_PAGE)
		{
			ArrayList<PageResult> list = PageResults;
			PaginationList.add(list);
		}
		else
		{

		}
	}


	@RequestMapping(value = "/SearchWordRequest", method = RequestMethod.POST)
	public ModelAndView save(@ModelAttribute Page page)
	{

		this.Database = new MongoDatabase("SearchEngineDatabase");
		ArrayList<String> QueryWords =  new ArrayList<String>(Arrays.asList(page.getName().split(" ")));
		PorterStemmer porterStemmer = new PorterStemmer();

		for (int i = 0; i < QueryWords.size(); i++)
		{
			String StemmedQuery = porterStemmer.stemWord(QueryWords.get(i));
			QueryWords.set(i, StemmedQuery);
		}

		ArrayList<PageResult> PageResults = new ArrayList<>();
		PageResults = retrievePagesFromQuery(QueryWords);

		PageRanker Ranker = new PageRanker(PageResults);
		PageResults = Ranker.getRankedPages();


		//if none is returned display no results found page
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("no_results");
		if(PageResults.size()== 0)
			return modelAndView;


		System.out.println("Input from UI = " + page.getName());
		System.out.println("retreived pages = " + PageResults.get(0).getURL());
		//display array

		List<Page> returnedPages = new LinkedList<Page>();
		int i = 0;
		for(PageResult PR : PageResults)
		{
			Page np = new Page();
			np.setURL(PR.getURL().toString());
			np.setName(PR.getTitle().toString());
			int size = PR.getContent().toString().length();
			if(size < 700)
				np.setContent(PR.getContent().toString().substring(0,size));
			else
				np.setContent((PR.getContent().toString().substring(0,700)+"......"));
			returnedPages.add(i,np);
			i++;
		}
		Collections.reverse(returnedPages);


		modelAndView.setViewName("display_results");
		modelAndView.addObject("pages", returnedPages);
		return modelAndView;
	}

}
