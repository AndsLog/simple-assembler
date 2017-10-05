import java.util.*;
import java.io.*;

public class Test2{
    static int loc=0;//location number
    static int line=0;//行號
    static int start=0;//程式起始位置
	static int end=0;//程式結束位置
	static int total_leng=0;//總長度
	static int error_num=0;//總錯誤次數
	static String loc_to_s=null;//將loc轉成16進位的String
	static String total_to_s=null;//將total length轉成16進位的String
    static String[][] symbol_tab=new String[100][2];//建立symbol table
    static int sym_index=0;//symbol table的索引
    static String prog_name=null;//紀錄program name
	static String obj_all="";//存放object code
	static String obj_temp="";
	static int obj_total_leng=0;
	static String end_code=null;
	static String h_ccode=null;
	static String line_srt=null;//存查op_tab sym_tab的結果
    public static void main(String[] argv) throws IOException{
		//pass 1
        FileReader fr = new FileReader("test.txt");
        BufferedReader br = new BufferedReader(fr);//讀檔
        FileWriter fw = new FileWriter("out2.txt");
        BufferedWriter bw = new BufferedWriter(fw);//寫檔
        String Br;
        while((Br=br.readLine())!=null){//資料不為空值進入迴圈
            line++;
			int token_num=0;//紀錄有幾個token
			int token_point=0;//紀錄有幾個","
			String op_rsu=null;//紀錄"RSUB"的opcode
			String total_s=null;//總長度的16進位
			String loc_s=null;//location number的16進位
            String cut_line=null;//存放清除註解後的String
            cut_line=clearComment(Br);//將檔案資料放入函數去除註解
            String[] token=cut_line.split("\\s+");
            for(int i=0;i<token.length;i++){
			    token_num++;
				token_point=token[i].indexOf(",");
				if((token_point>=0)&&(!token[i].endsWith(",X"))){//[opreand],X 索引定址 判斷"," 及 "X"的個數是否正確
				    if(((token.length==3)&&(token[2].indexOf("X")>=0))){//1.token[1]=buffer, token[2]=X 2.token[1]=buffer token[2]=,X
						if(token[i].endsWith(",")){//情況1 ","在operand後面
							token[i]=token[i].concat(token[i+1]);
						    break;
						}
						else if(token[i+1].startsWith(",")){//情況2 ","在X前面
							token[i]=token[i].concat(token[i+1]);
							break;
						}
					}
					else if(((token.length==4)&&(token[3].indexOf("X")>=0))){//token[1]=[operand] token[2]=, token[3]=X
						if(token[i+1].startsWith(",")){
							token[i+1]=token[i+1].concat(token[i+2]);
							token[i]=token[i].concat(token[i+1]);
							break;
						}
					}
					else{
					    System.out.println("error==>"+line+": 索引定址格式錯誤");
						error_num++;
					}
				}
			}
			if(token_num>3){
				System.out.println("error==>"+line+": 格式錯誤");
				error_num++;
			}
			else if(token_num==3){//有 [label] [opcode] [operand]
				if(token[1].equals("START")){//若為START
				    check_start(token[0],token[1],token[2],bw);
					continue;
				}
				else if(token[1].equals("END")){//為EDN 計算總長度 印出最後一行
					end=loc;
					total_leng=end-start;
					total_to_s=Integer.toHexString(total_leng);//轉成16進位
					loc_to_s=Integer.toHexString(loc);//轉成16進位
					bw.write(line+": "+loc_to_s+" "+token[0]+" "+token[1]+" "+token[2]);
					bw.newLine();
					break;
				}
				else if(token[1].equals("RSUB")){//[label] "RSUB" [operand]
					if(!token[2].equals("")){
						System.out.println("error==>"+line+":RSUB不能有operand");
						error_num++;
					}
					continue;
				}
				else{
					line_srt=null;
					loc_to_s=Integer.toHexString(loc);//轉成16進位
					bw.write(line+": "+loc_to_s+" ");
					line_srt=check_sym(token[0],bw)+check_optab(token[1],token[2],bw);
					bw.write(line_srt);
					bw.newLine();
					continue;
				}
			}
			else if(token_num==2){//有 [opcode] [operand]
			    if(token[0].equals("END")){
					end=loc;
					total_leng=end-start;
					total_to_s=Integer.toHexString(total_leng);//轉成16進位
					loc_to_s=Integer.toHexString(loc);//轉成16進位
					bw.write(line+": "+loc_to_s+" "+null+" "+token[0]+" "+token[1]);
					bw.newLine();
					break;
				}
				else if(!token[1].equals("RSUB")){
					line_srt=null;
					loc_to_s=Integer.toHexString(loc);//轉成16進位
					bw.write(line+": "+loc_to_s+" ");
					line_srt=null+" "+check_optab(token[0],token[1],bw);
					bw.write(line_srt);
					bw.newLine();
					continue;
				}
				else{// 有 [label] [opcode]="RSUB"
				    loc_to_s=Integer.toHexString(loc);//轉成16進位
					bw.write(line+": "+loc_to_s+" ");
					if(token[0].equals("RSUB")){//"RSUB" [operand]
						System.out.println("error==>"+line+":RSUB不能有operand");
						error_num++;
						continue;
					}
					else if(token[1].equals("RSUB")){
						line_srt=null;
					    op_rsu=search(token[1]);
					    line_srt=check_sym(token[0],bw)+" "+op_rsu;
					    bw.write(line_srt);
				        loc=loc+3;
					    bw.newLine();
					}
					continue;
				}
			}
			else if(((token_num==1)&&(token[0].equals("RSUB")))){// "RSUB"
				loc_to_s=Integer.toHexString(loc);//轉成16進位
				op_rsu=search(token[0]);
				bw.write(line+": "+loc_to_s+" null "+op_rsu+" null");
				loc=loc+3;
				bw.newLine();
				continue;
			}
            bw.flush();
            bw.newLine();
            continue;
        }
        fr.close();
        bw.close();
		//pass 2
	    FileReader fr2 = new FileReader("out.txt");
	    BufferedReader br2 = new BufferedReader(fr2);//讀中間檔
	    FileWriter fw2 = new FileWriter("object.txt");
        BufferedWriter bw2 = new BufferedWriter(fw2);//寫檔
	    String Br2;
		line=0;
	    while((Br2=br2.readLine())!=null){//資料不為空值進入迴圈
			line++;
			int token2_num=0;//token[]長度
			int sta=2;//不用判斷的token 個數 token[0]=line token[1]=loc 可以略過
			int ind_addr=0;//記錄是否為索引定址
	        if(Br2.trim().isEmpty())
			    continue;
		    else{
			    String[] token2=Br2.split("\\s+");
			    for(int i=0;i<token2.length;i++){
				    token2_num++;
			    }
				if(token2_num==(sta+3)){// [label] [opcode] [operand]
					sta--;//把token 個數換成array index，token[1]->有2個
					ind_addr=token2[sta+3].indexOf(",X");
					if(token2[sta+2].equals("START")){//寫出 h recode 
						h_ccode=h_recode(prog_name,start,total_leng,bw2);
						continue;
					}
					else if(token2[sta+2].equals("END")){//寫出 e recode
						end_code=e_recode(token2[sta+2],bw2);
						continue;
					}
					else if(token2[sta+2].equals("WORD")||token2[sta+2].equals("RESB")||token2[sta+2].equals("RESW")||token2[sta+2].equals("BYTE")){
						data_recode(token2[1],token2[sta+2],token2[sta+3],bw2);
						continue;
					}
					else if(ind_addr>=0){//判斷索引定址
						index_addr(token2[1],token2[sta+2],token2[sta+3],bw2);
						continue;
					}
					else{
						t_recode(token2[1],token2[sta+2],token2[sta+3],bw2);
					}
				}
				else if(token2_num==(sta+2)){//[opcode] [operand]
				    sta--;
					ind_addr=token2[sta+2].indexOf(",X");
					if(token2[sta+1].equals("END")){//寫出 e recode
					    end_code=e_recode(token2[sta+2],bw2);
						continue;
					}
					else if(token2[sta+1].equals("WORD")||token2[sta+1].equals("RESB")||token2[sta+1].equals("RESW")||token2[sta+1].equals("BYTE")){
						data_recode(token2[1],token2[sta+1],token2[sta+2],bw2);
						continue;
					}
					else if(token2[sta+2].equals("4C")){//[label] RSUB
					    String rsub=null;
						rsub="4C0000";
						print_all(token2[1],rsub);
						continue;
					}
					else if(ind_addr>=0){//判斷索引定址
						index_addr(token2[1],token2[sta+1],token2[sta+2],bw2);
						continue;
					}
					else{
						t_recode(token2[1],token2[sta+1],token2[sta+2],bw2);
						continue;
					}
				}
				else{ // RSUB
				    String rsub=null;
					rsub="4C0000";
					print_all(token2[1],rsub);
					continue;
				}
	        }
        }
	    if(error_num>0){
		    System.out.println("error:"+error_num);
		    System.exit(1);
	    }
		bw2.write(h_ccode);
		bw2.newLine();
		System.out.println(h_ccode);
		bw2.write(obj_all);
		System.out.print(obj_all);
		bw2.write(end_code);
		System.out.print(end_code);
        fr2.close();
        bw2.close();
    }
//method
	
	public static void print_all(String loc,String obj_code){
		int op_leng=obj_code.length()/2;
		if(obj_total_leng+op_leng>30){
			obj_all=obj_all+" "+String.format("%02X",obj_total_leng)+" "+obj_temp+"\r\n";
			obj_temp="";
			obj_total_leng=0;
		}
		if(obj_total_leng==0){
			if(loc.length()<6)
				loc=long_size(loc);
			obj_all=obj_all+"T"+" "+loc;
		}
		obj_temp=obj_temp+obj_code;
		obj_total_leng=obj_total_leng+op_leng;
	}
	public static void index_addr(String loc,String opcode,String operand,BufferedWriter bw2) throws IOException{//判斷索引定址
		BufferedWriter Bw2=bw2;
		int ind_op=0;//記錄在","之前的operand
		int ind_in=0;//將location轉成int
		String ind_op_str=null;//","之前的operand
		String ind_res=null;//記錄[label] 的 location
		String index_loc=null;
		ind_op=operand.indexOf(",");
		ind_op_str=operand.substring(0,ind_op);//取出","前的operand
		ind_res=search_sym(ind_op_str);
		if(ind_res.indexOf("error")!=-1)
			System.out.println(ind_res);
		ind_in=Integer.parseInt(ind_res,16)+32768;//索引定址
		index_loc=Integer.toHexString(ind_in);//將加8000的位址轉成string
		index_loc=opcode+index_loc;//把opcode與location合併
		print_all(loc,index_loc);
	}
	public static String long_size(String obj_code){//將object code補滿6個
		String done="0";//存長度滿6的odject code
		if(((obj_code.length()<6)&&(done.length()<6))){
			for(int i=obj_code.length()+1;i<6;i++)
				done=done+"0";
			done=done+obj_code;
		}
		return done;
	}
	public static void data_recode(String loc,String opcode,String operand,BufferedWriter bw2) throws IOException{//處理 WORD RESW RESB BYTE
		BufferedWriter Bw2=bw2;
		String data_res=null;//存處理資料後的object code
		int byte_one=0,byte_two=0,word_oper=0;//紀錄","的位置及將operand轉成int
		if(opcode.equals("WORD")){
			word_oper=Integer.parseInt(operand);
			data_res=Integer.toHexString(word_oper);
		}
		else if(opcode.equals("RESW")||opcode.equals("RESB")){
			if(obj_total_leng!=0){
				obj_all=obj_all+" "+String.format("%02X",obj_total_leng)+" "+obj_temp+"\r\n";
			    obj_temp="";
			    obj_total_leng=0;
			}
			return;
		}
		else if(opcode.equals("BYTE")){
			int x_check=operand.indexOf("X\'");
			if(x_check>=0){
				byte_one=operand.indexOf("\'",0);
			    byte_two=operand.indexOf("\'",2);
				data_res=operand.substring(byte_one+1,byte_two);
				print_all(loc,data_res);
				return;
			}
			else{
				data_res=operand;
			}
		}
		if(data_res.length()<6)
			data_res=long_size(data_res);
		print_all(loc,data_res);
	}
	
	public static String h_recode(String name2,int start2,int total_leng2,BufferedWriter bw2) throws IOException{//判斷 h recode格式
		String p_name=null,p_start=null,p_total_leng=null,h_code=null;
		BufferedWriter Bw2=bw2;
		p_name=name2;
		p_start=Integer.toHexString(start2);//轉成16進位
		p_total_leng=Integer.toHexString(total_leng2);//轉成16進位
		if(p_name.length()<6)
		    p_name=long_size(p_name);
		else if(p_start.length()<6)
			p_start=long_size(p_start);
		else if (p_total_leng.length()<6)
			p_total_leng=long_size(p_total_leng);
		h_code="H"+p_name+"  "+p_start+" "+p_total_leng;
		return h_code;
	}
	public static void t_recode(String loc,String opcode,String operand,BufferedWriter bw2) throws IOException{//判斷 t recode格式
		BufferedWriter Bw2=bw2;
		String obj_code=null;
		String sea_res=null;//存operand對應的location
		sea_res=search_sym(operand);
		if(sea_res.indexOf("error")!=-1)
			System.out.println(sea_res);
		obj_code=opcode+sea_res;
		if(obj_code.length()<6)
		    obj_code=long_size(obj_code);
		print_all(loc,obj_code);
	}
	public static String e_recode(String operand,BufferedWriter bw2) throws IOException{//判斷 e recode格式
		BufferedWriter Bw2=bw2;
		String end_recode=null;
		String start_pos=null;//程式開始位址
		start_pos=search_sym(operand);
		if(start_pos.indexOf("error")!=-1)
			System.out.println(start_pos);
		if(start_pos.length()<6)
		    start_pos=long_size(start_pos);
		if(obj_total_leng!=0){
			obj_all=obj_all+" "+String.format("%02X",obj_total_leng)+" "+obj_temp+"\r\n";
			obj_temp="";
			obj_total_leng=0;
		}
		end_recode="E"+start_pos;
		return end_recode;
	}
	public static void check_start(String name,String ST,String begin){//抓出program name 及 開始點
        String loc_to_s=null;
		BufferedWriter Bw=bw;
		loc=Integer.parseInt(begin,16);//設定loc開始的地方，將begin轉成16進位
        start=Integer.parseInt(begin,16);//將開始點存入start用以計算長度
		loc_to_s=Integer.toHexString(loc);//轉成16進位
		if(name.length()<=6)//判斷program name長度 
		    prog_name=name;//記錄program name
		else{
			System.out.println("error==>"+line+":"+"program name長度超過6byte");
			error_num++;
		}
        Bw.write(line+": "+loc_to_s+" "+name+" "+ST+" "+begin);//把token[i-1](program name) 寫入中間檔
		bw.newLine();
	}
	public static String check_optab(String opcode,String operand){//判斷 op code
	    BufferedWriter Bw=bw;
		String op_code_sta=null;
		String op_res=null;//存放查詢opcode table的結果
		String op_code=null;//存放查詢opcode table後的opcode
		int byte_long=0;//計算BYTE的長度
		op_res=search(opcode);//查詢opcode table
	    if(!op_res.equals("miss")){
            loc=loc+3;
            op_code=op_res;
        }
        else if(opcode.equals("WORD")){
            loc=loc+3;
        }
        else if(opcode.equals("RESW")){
            loc=loc+Integer.parseInt(operand,16)*3;
        }
        else if(opcode.equals("RESB")){
            loc=loc+Integer.parseInt(operand,10);
        }
        else if(opcode.equals("BYTE")){
			String asc_res=null;//string轉成ascii code的結果
            if(operand.startsWith("C")){//為字元型態C
				asc_res=to_asc(operand);
				operand=asc_res;
                byte_long=(operand.indexOf("\'",2)-operand.indexOf("\'",0)-1);//C'_ _ _'用前後引號求出BYTE的長度
				loc=loc+byte_long;//一個字元為一個byte
            }
            if(operand.startsWith("X")){//為數字型態X
                byte_long=(operand.indexOf("\'",2)-operand.indexOf("\'",0))-1;
				if(byte_long%2==0){
                    loc=loc+(byte_long/2);//數字兩個為一個byte，故除以2
				}
				else{
					System.out.println("error==>"+line+":"+operand+"不為偶數長度"+" byte_long:"+byte_long);
				    error_num++;
				}
            }
        }
		else{
			System.out.println("error==>"+line+":"+opcode+"定義錯誤");
			error_num++;
		}
		op_code_sta=opcode+" "+operand;
		return op_code_sta;
	}
	public static String to_asc(String operand){//把C型態的 string 轉成ascii code
	    int two_point=0,one_point=0;//第二個"'" 與 第一個"'" 的位址
		String get_str=null;//存'_ _ _'引號中的文字
		int ch_leng=0;//char array的長度
		two_point=operand.indexOf("\'",2);
		one_point=operand.indexOf("\'",0);
		get_str=operand.substring(one_point+1,two_point);
		char [] ch_to_str=get_str.toCharArray();//將str轉成char
		for(int i=0;i<ch_to_str.length;i++){
			ch_leng++;
		}
		int [] ch_asc=new int [ch_leng];//儲存轉成ascii的char
		String temp=null;
		String [] str_asc=new String [ch_leng];//將ascii轉成16進位
		String res="";//將srt_asc array中 所有文字相加
		for(int i=0;i<ch_leng;i++){
			ch_asc[i]=(int)ch_to_str[i];
			temp=Integer.toHexString(ch_asc[i]);
			str_asc[i]=temp;
			res=res+str_asc[i];
		}
		return res;
	}
	public static String check_sym(String symbol){//檢查symbol table中有無重複的symbol 及 是否是保留字元
		String symbol_sta=null;
		boolean check=false;
		int s=sym_index+1;
		s--;//回推到symbol table最後放入的資料
		String check_op=null;
		check_op=search(symbol);//查詢symbol是否為保留字元
		if(check_op.equals("miss")){
		    while((check!=true)&&(s>=0)){
                if(sym_index==0){//symbol table中沒有值，直接把token[0](label) 及 location 放入symbol table
                    symbol_tab[sym_index][0]=symbol;
					String loc_st=Integer.toHexString(loc);
                    symbol_tab[sym_index][1]=loc_st;
                    sym_index++;
					Bw.write(symbol+" ");
                    break;
                }
                else{//symbol_table目前存到第S列時
                    check=symbol.equals(symbol_tab[s][0]);
                    s--;
                    if(check!=true){//沒有重複定義，把symbol(label)放入symbol table
                        symbol_tab[sym_index][0]=symbol;
						String loc_st=Integer.toHexString(loc);
                        symbol_tab[sym_index][1]=loc_st;
					    if(s==0){//從第S列往回找，找到第1列都沒有重複定義的話symbol table的索引(sym_index)+1
						    sym_index++;
							Bw.write(symbol+" ");
                        }
				    }
				    else{
					    System.out.println("error==>"+line+":"+symbol+"重複定義");//重複定義，印出錯誤訊息
						error_num++;
					}
                }
            }
			symbol_sta=symbol+" ";
		}
		else{
			System.out.println("error==>"+line+":"+symbol+"為保留字元");
			error_num++;
		}
		return symbol_sta;
	}
    public static String clearComment(String line){//用indexOf清除註解
        int point_NO=0;
        String line_point=null;
        point_NO=line.indexOf(".");//紀錄此行資料中有幾個"."
        if(point_NO!=-1)//有"."的情況
            line_point=line.substring(0,point_NO);
        else//沒有"."的情況
            line_point=line;
        return line_point;
    }
	public static String search_sym(String operand){//找出symbol table中 [label] 對應的location
		String ans=null;
		boolean equal=false;
		int s=sym_index;
		int flag=0;
		while((equal!=true)&&(s>=0)){
			equal=operand.equals(symbol_tab[s][0]);
			
			if(equal==true){//找到相等的label
				ans=symbol_tab[s][1];
				flag=1;
				break;
			}
			s--;
		}
		if(flag==0){
			ans="error==>"+line+": symbol undefine";
			error_num++;
		}
		return ans;
	}
    public static String search(String code){ //search opcode method 
        String ans=null;
        String [][] inst_table={
            {"ADD","18"},
            {"ADDF","58"},
            {"ADDR","90"},
            {"AND","40"},
            {"CLEAR","B4"},
            {"COMP","28"},
            {"COMPF","88"},
            {"COMPR","A0"},
            {"DIV","24"},
            {"DIVF","64"},
            {"DIVR","9C"},
            {"FIX","C4"},
            {"FLOAT","C0"},
            {"HIO","F4"},
            {"J","3C"},
            {"JEQ","30"},
            {"JGT","34"},
            {"JLT","38"},
            {"JSUB","48"},
            {"LDA","00"},
            {"LDB","68"},
            {"LDCH","50"},
            {"LDF","70"},
            {"LDL","08"},
            {"LDS","6C"},
            {"LDT","74"},
            {"LDX","04"},
            {"LPS","D0"},
            {"MUL","20"},
            {"MULF","60"},
            {"MULR","98"},
            {"NORM","C8"},
            {"OR","44"},
            {"RD","D8"},
            {"RMO","AC"},
            {"RSUB","4C"},
            {"SHIFTL","A4"},
            {"SHIFTR","A8"},
            {"SIO","F0"},
            {"SSK","EC"},
            {"STA","0C"},
            {"STB","78"},
            {"STCH","54"},
            {"STF","80"},
            {"STI","D4"},
            {"STL","14"},
            {"STS","7C"},
            {"STSW","E8"},
            {"STT","84"},
            {"STX","10"},
            {"SUB","1C"},
            {"SUBF","5C"},
            {"SUBR","94"},
            {"SVC","B0"},
            {"TD","E0"},
            {"TIO","F8"},
            {"TIX","2C"},
            {"TIXR","B8"},
            {"WD","DC"},
        };
        boolean equal=false;
        int i=0;
        while((equal!=true)&&(i<inst_table.length)){//若還沒有 找到符合的 及 i比inst_table的行數還小 就繼續往下找
            equal=code.equals(inst_table[i][0]);//比對 code 與 inst_table第i行第1個值 是否相等
            if(equal==true) //若相等則為true
                ans=inst_table[i][1];//將inst_table第i行第2個值 給ans
            else//若不相等
                ans="miss";//回傳原資料
            i++;
        }
        return ans;
    }
}