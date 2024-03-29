ActionController::Routing::Routes.draw do |map|
  # The priority is based upon order of creation: first created -> highest priority.
  map.root :controller => :home, :action => :index
  
  map.sync_get  '/feeds/:guid', :conditions => {:method => :get},  :controller => :feeds, :action => :index
  map.sync_post '/feeds/:guid', :conditions => {:method => :post}, :controller => :feeds, :action => :sync
  map.schema_post 'feeds/:guid/schema', :conditions => {:method => :post}, :controller => :feeds, :action => :save_schema
  map.schema_get '/feeds/:guid/schema', :conditions => {:method => :get}, :controller => :feeds, :action => :schema

  map.create_mesh '/meshes/:name', :conditions => {:method => :post}, :controller => :meshes, :action => :create
  map.show_mesh '/meshes/:name', :conditions => {:method => :get}, :controller => :meshes, :action => :show
  map.delete_mesh '/meshes/:name/delete', :conditions => {:method => :get}, :controller => :meshes, :action => :delete

  map.create_feed '/meshes/:mesh_name/feeds/:feed_name', :conditions => {:method => :post}, :controller => :feeds, :action => :create
  map.delete_feed '/meshes/:mesh_name/feeds/:feed_name/delete', :conditions => {:method => :get}, :controller => :feeds, :action => :delete

  # Install the default routes as the lowest priority.
  # Note: These default routes make all actions in every controller accessible via GET requests. You should
  # consider removing or commenting them out if you're using named routes and resources.
  map.connect ':controller/:action/:id'
  map.connect ':controller/:action/:id.:format'
end
