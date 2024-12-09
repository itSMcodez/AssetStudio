package com.itsmcodez.assetstudio.models;

import com.itsmcodez.assetstudio.markers.ModelType;
import com.itsmcodez.assetstudio.models.base.Model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SearchModel implements Model {

    @SuppressWarnings("unchecked")
    public SearchModel(CharSequence constraint,  List<? extends Model> source, boolean ignoreCase) {
        SearchResult result = new SearchResult();
        ArrayList<Model> models = new ArrayList<>(source);
        final ArrayList<Model> FILTRATE = new ArrayList<>();
        final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
        
        // 1. start filtering
        if(models != null) {
            onSearch(constraint, true);
            
            // 2. return original list if there is no constraint/limiter
        	if(constraint == null || constraint.length() == 0) {
                result.setValue(models);
                result.setResultCount(models.size());
            } else {
                // 3. filter target list
                try {
                    EXECUTOR.execute(() -> {
                            synchronized(models) {
                                for(Model model : models) {
                                        CharSequence name = null;
                                        if(model != null) {
                                        	switch(model.getModelType()) {
                                                case ICON_MODEL : {
                                                    IconModel icon = (IconModel) model;
                                                    name = icon.getName();
                                                    assert name != null;
                                                    if(ignoreCase) {
                                                        if (name.toString().toLowerCase().contains(constraint.toString().toLowerCase()) )
                                                            FILTRATE.add(icon);
                                                    } else {
                                                        if(name.toString().contains(constraint.toString()))
                                                            FILTRATE.add(icon); 
                                                    };
                                                }
                                                    break;
                                                case SEARCH_MODEL : {
                                                    SearchModel search = (SearchModel) model;
                                                    name = search.getClass().getSimpleName();
                                                    assert name != null;
                                                    if(ignoreCase) {
                                                        if (name.toString().toLowerCase().contains(constraint.toString().toLowerCase()) )
                                                            FILTRATE.add(search);
                                                    } else {
                                                        if(name.toString().contains(constraint.toString()))
                                                            FILTRATE.add(search); 
                                                    };
                                                }
                                                    break;
                                                default :
                                                    name = null;
                                            }
                                        }
                                };
                            }
                    });
                    
                    // 4. set results
            		result.setValue(FILTRATE);
            		result.setResultCount(FILTRATE.size());
                    
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    EXECUTOR.shutdown();
                }
            }
            
            // 5. post results
            onPublishResult(result);
        }
    }

    public abstract void onPublishResult(SearchResult result);
    
    public abstract void onSearch(CharSequence constraint, boolean isRunning);

    @Override
    public ModelType getModelType() {
        return ModelType.SEARCH_MODEL;
    }

    public class SearchResult {
        private List<? extends Model> value;
        private int resultCount;

        public List<? extends Model> getValue() {
            return this.value;
        }

        public void setValue(List<? extends Model> value) {
            this.value = value;
        }

        public int getResultCount() {
            return this.resultCount;
        }

        public void setResultCount(int resultCount) {
            this.resultCount = resultCount;
        }
    }
}
