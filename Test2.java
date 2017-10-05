import java.util.*;
import java.io.*;

public class Test2{
    static int loc=0;//location number
    static int line=0;//�渹
    static int start=0;//�{���_�l��m
	static int end=0;//�{��������m
	static int total_leng=0;//�`����
	static int error_num=0;//�`���~����
	static String loc_to_s=null;//�Nloc�ন16�i�쪺String
	static String total_to_s=null;//�Ntotal length�ন16�i�쪺String
    static String[][] symbol_tab=new String[100][2];//�إ�symbol table
    static int sym_index=0;//symbol table������
    static String prog_name=null;//����program name
	static String obj_all="";//�s��object code
	static String obj_temp="";
	static int obj_total_leng=0;
	static String end_code=null;
	static String h_ccode=null;
	static String line_srt=null;//�s�dop_tab sym_tab�����G
    public static void main(String[] argv) throws IOException{
		//pass 1
        FileReader fr = new FileReader("test.txt");
        BufferedReader br = new BufferedReader(fr);//Ū��
        FileWriter fw = new FileWriter("out2.txt");
        BufferedWriter bw = new BufferedWriter(fw);//�g��
        String Br;
        while((Br=br.readLine())!=null){//��Ƥ����ŭȶi�J�j��
            line++;
			int token_num=0;//�������X��token
			int token_point=0;//�������X��","
			String op_rsu=null;//����"RSUB"��opcode
			String total_s=null;//�`���ת�16�i��
			String loc_s=null;//location number��16�i��
            String cut_line=null;//�s��M�����ѫ᪺String
            cut_line=clearComment(Br);//�N�ɮ׸�Ʃ�J��ƥh������
            String[] token=cut_line.split("\\s+");
            for(int i=0;i<token.length;i++){
			    token_num++;
				token_point=token[i].indexOf(",");
				if((token_point>=0)&&(!token[i].endsWith(",X"))){//[opreand],X ���ީw�} �P�_"," �� "X"���ӼƬO�_���T
				    if(((token.length==3)&&(token[2].indexOf("X")>=0))){//1.token[1]=buffer, token[2]=X 2.token[1]=buffer token[2]=,X
						if(token[i].endsWith(",")){//���p1 ","�boperand�᭱
							token[i]=token[i].concat(token[i+1]);
						    break;
						}
						else if(token[i+1].startsWith(",")){//���p2 ","�bX�e��
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
					    System.out.println("error==>"+line+": ���ީw�}�榡���~");
						error_num++;
					}
				}
			}
			if(token_num>3){
				System.out.println("error==>"+line+": �榡���~");
				error_num++;
			}
			else if(token_num==3){//�� [label] [opcode] [operand]
				if(token[1].equals("START")){//�Y��START
				    check_start(token[0],token[1],token[2],bw);
					continue;
				}
				else if(token[1].equals("END")){//��EDN �p���`���� �L�X�̫�@��
					end=loc;
					total_leng=end-start;
					total_to_s=Integer.toHexString(total_leng);//�ন16�i��
					loc_to_s=Integer.toHexString(loc);//�ন16�i��
					bw.write(line+": "+loc_to_s+" "+token[0]+" "+token[1]+" "+token[2]);
					bw.newLine();
					break;
				}
				else if(token[1].equals("RSUB")){//[label] "RSUB" [operand]
					if(!token[2].equals("")){
						System.out.println("error==>"+line+":RSUB���঳operand");
						error_num++;
					}
					continue;
				}
				else{
					line_srt=null;
					loc_to_s=Integer.toHexString(loc);//�ন16�i��
					bw.write(line+": "+loc_to_s+" ");
					line_srt=check_sym(token[0],bw)+check_optab(token[1],token[2],bw);
					bw.write(line_srt);
					bw.newLine();
					continue;
				}
			}
			else if(token_num==2){//�� [opcode] [operand]
			    if(token[0].equals("END")){
					end=loc;
					total_leng=end-start;
					total_to_s=Integer.toHexString(total_leng);//�ন16�i��
					loc_to_s=Integer.toHexString(loc);//�ন16�i��
					bw.write(line+": "+loc_to_s+" "+null+" "+token[0]+" "+token[1]);
					bw.newLine();
					break;
				}
				else if(!token[1].equals("RSUB")){
					line_srt=null;
					loc_to_s=Integer.toHexString(loc);//�ন16�i��
					bw.write(line+": "+loc_to_s+" ");
					line_srt=null+" "+check_optab(token[0],token[1],bw);
					bw.write(line_srt);
					bw.newLine();
					continue;
				}
				else{// �� [label] [opcode]="RSUB"
				    loc_to_s=Integer.toHexString(loc);//�ন16�i��
					bw.write(line+": "+loc_to_s+" ");
					if(token[0].equals("RSUB")){//"RSUB" [operand]
						System.out.println("error==>"+line+":RSUB���঳operand");
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
				loc_to_s=Integer.toHexString(loc);//�ন16�i��
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
	    BufferedReader br2 = new BufferedReader(fr2);//Ū������
	    FileWriter fw2 = new FileWriter("object.txt");
        BufferedWriter bw2 = new BufferedWriter(fw2);//�g��
	    String Br2;
		line=0;
	    while((Br2=br2.readLine())!=null){//��Ƥ����ŭȶi�J�j��
			line++;
			int token2_num=0;//token[]����
			int sta=2;//���ΧP�_��token �Ӽ� token[0]=line token[1]=loc �i�H���L
			int ind_addr=0;//�O���O�_�����ީw�}
	        if(Br2.trim().isEmpty())
			    continue;
		    else{
			    String[] token2=Br2.split("\\s+");
			    for(int i=0;i<token2.length;i++){
				    token2_num++;
			    }
				if(token2_num==(sta+3)){// [label] [opcode] [operand]
					sta--;//��token �Ӽƴ���array index�Atoken[1]->��2��
					ind_addr=token2[sta+3].indexOf(",X");
					if(token2[sta+2].equals("START")){//�g�X h recode 
						h_ccode=h_recode(prog_name,start,total_leng,bw2);
						continue;
					}
					else if(token2[sta+2].equals("END")){//�g�X e recode
						end_code=e_recode(token2[sta+2],bw2);
						continue;
					}
					else if(token2[sta+2].equals("WORD")||token2[sta+2].equals("RESB")||token2[sta+2].equals("RESW")||token2[sta+2].equals("BYTE")){
						data_recode(token2[1],token2[sta+2],token2[sta+3],bw2);
						continue;
					}
					else if(ind_addr>=0){//�P�_���ީw�}
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
					if(token2[sta+1].equals("END")){//�g�X e recode
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
					else if(ind_addr>=0){//�P�_���ީw�}
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
	public static void index_addr(String loc,String opcode,String operand,BufferedWriter bw2) throws IOException{//�P�_���ީw�}
		BufferedWriter Bw2=bw2;
		int ind_op=0;//�O���b","���e��operand
		int ind_in=0;//�Nlocation�নint
		String ind_op_str=null;//","���e��operand
		String ind_res=null;//�O��[label] �� location
		String index_loc=null;
		ind_op=operand.indexOf(",");
		ind_op_str=operand.substring(0,ind_op);//���X","�e��operand
		ind_res=search_sym(ind_op_str);
		if(ind_res.indexOf("error")!=-1)
			System.out.println(ind_res);
		ind_in=Integer.parseInt(ind_res,16)+32768;//���ީw�}
		index_loc=Integer.toHexString(ind_in);//�N�[8000����}�নstring
		index_loc=opcode+index_loc;//��opcode�Plocation�X��
		print_all(loc,index_loc);
	}
	public static String long_size(String obj_code){//�Nobject code�ɺ�6��
		String done="0";//�s���׺�6��odject code
		if(((obj_code.length()<6)&&(done.length()<6))){
			for(int i=obj_code.length()+1;i<6;i++)
				done=done+"0";
			done=done+obj_code;
		}
		return done;
	}
	public static void data_recode(String loc,String opcode,String operand,BufferedWriter bw2) throws IOException{//�B�z WORD RESW RESB BYTE
		BufferedWriter Bw2=bw2;
		String data_res=null;//�s�B�z��ƫ᪺object code
		int byte_one=0,byte_two=0,word_oper=0;//����","����m�αNoperand�নint
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
	
	public static String h_recode(String name2,int start2,int total_leng2,BufferedWriter bw2) throws IOException{//�P�_ h recode�榡
		String p_name=null,p_start=null,p_total_leng=null,h_code=null;
		BufferedWriter Bw2=bw2;
		p_name=name2;
		p_start=Integer.toHexString(start2);//�ন16�i��
		p_total_leng=Integer.toHexString(total_leng2);//�ন16�i��
		if(p_name.length()<6)
		    p_name=long_size(p_name);
		else if(p_start.length()<6)
			p_start=long_size(p_start);
		else if (p_total_leng.length()<6)
			p_total_leng=long_size(p_total_leng);
		h_code="H"+p_name+"  "+p_start+" "+p_total_leng;
		return h_code;
	}
	public static void t_recode(String loc,String opcode,String operand,BufferedWriter bw2) throws IOException{//�P�_ t recode�榡
		BufferedWriter Bw2=bw2;
		String obj_code=null;
		String sea_res=null;//�soperand������location
		sea_res=search_sym(operand);
		if(sea_res.indexOf("error")!=-1)
			System.out.println(sea_res);
		obj_code=opcode+sea_res;
		if(obj_code.length()<6)
		    obj_code=long_size(obj_code);
		print_all(loc,obj_code);
	}
	public static String e_recode(String operand,BufferedWriter bw2) throws IOException{//�P�_ e recode�榡
		BufferedWriter Bw2=bw2;
		String end_recode=null;
		String start_pos=null;//�{���}�l��}
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
	public static void check_start(String name,String ST,String begin){//��Xprogram name �� �}�l�I
        String loc_to_s=null;
		BufferedWriter Bw=bw;
		loc=Integer.parseInt(begin,16);//�]�wloc�}�l���a��A�Nbegin�ন16�i��
        start=Integer.parseInt(begin,16);//�N�}�l�I�s�Jstart�ΥH�p�����
		loc_to_s=Integer.toHexString(loc);//�ন16�i��
		if(name.length()<=6)//�P�_program name���� 
		    prog_name=name;//�O��program name
		else{
			System.out.println("error==>"+line+":"+"program name���׶W�L6byte");
			error_num++;
		}
        Bw.write(line+": "+loc_to_s+" "+name+" "+ST+" "+begin);//��token[i-1](program name) �g�J������
		bw.newLine();
	}
	public static String check_optab(String opcode,String operand){//�P�_ op code
	    BufferedWriter Bw=bw;
		String op_code_sta=null;
		String op_res=null;//�s��d��opcode table�����G
		String op_code=null;//�s��d��opcode table�᪺opcode
		int byte_long=0;//�p��BYTE������
		op_res=search(opcode);//�d��opcode table
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
			String asc_res=null;//string�নascii code�����G
            if(operand.startsWith("C")){//���r�����AC
				asc_res=to_asc(operand);
				operand=asc_res;
                byte_long=(operand.indexOf("\'",2)-operand.indexOf("\'",0)-1);//C'_ _ _'�Ϋe��޸��D�XBYTE������
				loc=loc+byte_long;//�@�Ӧr�����@��byte
            }
            if(operand.startsWith("X")){//���Ʀr���AX
                byte_long=(operand.indexOf("\'",2)-operand.indexOf("\'",0))-1;
				if(byte_long%2==0){
                    loc=loc+(byte_long/2);//�Ʀr��Ӭ��@��byte�A�G���H2
				}
				else{
					System.out.println("error==>"+line+":"+operand+"�������ƪ���"+" byte_long:"+byte_long);
				    error_num++;
				}
            }
        }
		else{
			System.out.println("error==>"+line+":"+opcode+"�w�q���~");
			error_num++;
		}
		op_code_sta=opcode+" "+operand;
		return op_code_sta;
	}
	public static String to_asc(String operand){//��C���A�� string �নascii code
	    int two_point=0,one_point=0;//�ĤG��"'" �P �Ĥ@��"'" ����}
		String get_str=null;//�s'_ _ _'�޸�������r
		int ch_leng=0;//char array������
		two_point=operand.indexOf("\'",2);
		one_point=operand.indexOf("\'",0);
		get_str=operand.substring(one_point+1,two_point);
		char [] ch_to_str=get_str.toCharArray();//�Nstr�নchar
		for(int i=0;i<ch_to_str.length;i++){
			ch_leng++;
		}
		int [] ch_asc=new int [ch_leng];//�x�s�নascii��char
		String temp=null;
		String [] str_asc=new String [ch_leng];//�Nascii�ন16�i��
		String res="";//�Nsrt_asc array�� �Ҧ���r�ۥ[
		for(int i=0;i<ch_leng;i++){
			ch_asc[i]=(int)ch_to_str[i];
			temp=Integer.toHexString(ch_asc[i]);
			str_asc[i]=temp;
			res=res+str_asc[i];
		}
		return res;
	}
	public static String check_sym(String symbol){//�ˬdsymbol table�����L���ƪ�symbol �� �O�_�O�O�d�r��
		String symbol_sta=null;
		boolean check=false;
		int s=sym_index+1;
		s--;//�^����symbol table�̫��J�����
		String check_op=null;
		check_op=search(symbol);//�d��symbol�O�_���O�d�r��
		if(check_op.equals("miss")){
		    while((check!=true)&&(s>=0)){
                if(sym_index==0){//symbol table���S���ȡA������token[0](label) �� location ��Jsymbol table
                    symbol_tab[sym_index][0]=symbol;
					String loc_st=Integer.toHexString(loc);
                    symbol_tab[sym_index][1]=loc_st;
                    sym_index++;
					Bw.write(symbol+" ");
                    break;
                }
                else{//symbol_table�ثe�s���S�C��
                    check=symbol.equals(symbol_tab[s][0]);
                    s--;
                    if(check!=true){//�S�����Ʃw�q�A��symbol(label)��Jsymbol table
                        symbol_tab[sym_index][0]=symbol;
						String loc_st=Integer.toHexString(loc);
                        symbol_tab[sym_index][1]=loc_st;
					    if(s==0){//�q��S�C���^��A����1�C���S�����Ʃw�q����symbol table������(sym_index)+1
						    sym_index++;
							Bw.write(symbol+" ");
                        }
				    }
				    else{
					    System.out.println("error==>"+line+":"+symbol+"���Ʃw�q");//���Ʃw�q�A�L�X���~�T��
						error_num++;
					}
                }
            }
			symbol_sta=symbol+" ";
		}
		else{
			System.out.println("error==>"+line+":"+symbol+"���O�d�r��");
			error_num++;
		}
		return symbol_sta;
	}
    public static String clearComment(String line){//��indexOf�M������
        int point_NO=0;
        String line_point=null;
        point_NO=line.indexOf(".");//���������Ƥ����X��"."
        if(point_NO!=-1)//��"."�����p
            line_point=line.substring(0,point_NO);
        else//�S��"."�����p
            line_point=line;
        return line_point;
    }
	public static String search_sym(String operand){//��Xsymbol table�� [label] ������location
		String ans=null;
		boolean equal=false;
		int s=sym_index;
		int flag=0;
		while((equal!=true)&&(s>=0)){
			equal=operand.equals(symbol_tab[s][0]);
			
			if(equal==true){//���۵���label
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
        while((equal!=true)&&(i<inst_table.length)){//�Y�٨S�� ���ŦX�� �� i��inst_table������٤p �N�~�򩹤U��
            equal=code.equals(inst_table[i][0]);//��� code �P inst_table��i���1�ӭ� �O�_�۵�
            if(equal==true) //�Y�۵��h��true
                ans=inst_table[i][1];//�Ninst_table��i���2�ӭ� ��ans
            else//�Y���۵�
                ans="miss";//�^�ǭ���
            i++;
        }
        return ans;
    }
}