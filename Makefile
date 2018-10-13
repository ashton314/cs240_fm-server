deploy-doc:
	rsync -aivz target/default/doc/ ashwiers@schizo.cs.byu.edu:public_html/classes/cs_240/fm-server/
