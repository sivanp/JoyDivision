import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class PathTokens{
		String path;
		String pre;
		String channel;
		int offset;
		
		int site;
		int frame;
		boolean isValid=false;
		
		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getPre() {
			return pre;
		}

		public void setPre(String pre) {
			this.pre = pre;
		}

		public int getSite() {
			return site;
		}

		public void setSite(int site) {
			this.site = site;
		}

		public int getFrame() {
			return frame;
		}

		public void setFrame(int frame) {
			this.frame = frame;
		}

	
		public PathTokens(ImageStack stack,int slice){
			this(stack.getSliceLabel(slice));			
		}
		
		
		// the path is sometimes the label which holds much more
		public PathTokens(String path){		
			if(!path.contains(".tif")){//this is not a tif file				
				return;
			}
			String[] pathparts = path.split(".tif");
			
			path=pathparts[0];
			String[] tokens= path.split("_");
			int n=tokens.length;
			if("img".compareTo(tokens[0])!=0){ //movie maker format
				pre="";
				for (int i=0; i<n-2; i++){
					pre+=tokens[i]+"_";
				}
				site=Integer.parseInt(tokens[n-2]);
				frame=Integer.parseInt(tokens[n-1]);
				isValid=true;
			}
			else{
				pre=tokens[0]+"_"+tokens[2]+"_"+tokens[3];
				site=-1;
				frame=Integer.parseInt(tokens[1]);
				channel=tokens[2];
				offset=Integer.valueOf(tokens[3]);
				isValid=true; 
			}
//			StringTokenizer tokenizer = new StringTokenizer(path,"._");
//			String token=tokenizer.nextToken();
//			pre="";
//			boolean isDigit=Character.isDigit(token.charAt(0));
//			while(!isDigit ){
//				pre+=token;
//				token=tokenizer.nextToken();
//				isDigit=Character.isDigit(token.charAt(0));
//			}
//			site=Integer.parseInt(token);
//			token=tokenizer.nextToken();
//			frame=Integer.parseInt(token);
		}
		
		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public String getChannel() {
			return channel;
		}

		public void setChannel(String channel) {
			this.channel = channel;
		}

		public static int getCurFrame(ImagePlus imp){
			ImageStack stack=imp.getStack();
			String label=stack.getSliceLabel(imp.getCurrentSlice());
			PathTokens pt=new PathTokens(label);
			return pt.getFrame();
		}
	}
