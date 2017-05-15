/*
Written by LiuRunky on 14th, May, 2017
This program is open-source and non-commercial
Please leave this paragraph if you copy the code
And give me the least amount of respect
Thank you for your cooperation

I write the text above, pretending I am serious
owo
*/
#include <iostream>
#include <cstring>
#include <cmath>
#include <algorithm>
#include <string>
#include <fstream>
#include <vector>
#include <iomanip>
using namespace std;

const int BLOCKSIZE=192;
string InputFilename,OutputFilename;
double Multiplier;
ifstream fin;
ofstream fout;

inline int toint(string str)
{
    int val=0;
    int i=0;
    int status=1;
    if(str[0]=='-')
        i++,status=-1;
    
    for(;i<str.length();i++)
        val*=10,val+=(int)(str[i]-'0');
    
    return status*val;
}

inline double todouble(string str)
{
    int i=0;
    double val=0.0;
    int status=1;
    if(str[0]=='-')
        i++,status=-1;
    
    for(;i<str.length();i++)
    {
        if(str[i]=='.')
            break;
        val=val*10.0,val+=(double)(str[i]-'0');
    }
    
    double unit=1;
    for(i++;i<str.length();i++)
    {
        unit/=10.0;
        val+=unit*(int)(str[i]-'0');
    }
    
    return val*status;
}

inline string getHandle(string str)
{
    for(int i=0;i<str.length();i++)
        if(str[i]==':')
            return str.substr(0,i);
}

inline string getVal(string str,int type)
{
    int i;
    for(i=0;i<str.length();i++)
        if(str[i]==':')
            break;
    for(i++;i<str.length();i++)
        if(str[i]!=' ')
            break;
    
    return str.substr(i,(int)str.length()-i-type);
}

inline string getIdx(string str)
{
    for(int i=0;i<str.length();i++)
        if(str[i]==':')
            return str.substr(1,i-1);
}

inline int tonext(string str,int start)
{
    for(int i=start;i<str.length();i++)
        if(str[i]==',' || str[i]==':')
            return i;
    return (int)str.length();
}

struct osu
{
//General
    string AudioFilename,
           SampleSet;
    int    AudioLeadIn,
        PreviewTime,
        Countdown,
        Mode,
        LetterboxInBreaks,
        SpecialStyle,
        WidescreenStoryboard;
    double StackLeniency;

//Editor
    int BeatDivisor,
        GridSize;
    double DistanceSpacing,
           TimelineZoom;

//Metadata
    string Title,
           TitleUnicode,
           Artist,
           ArtistUnicode,
           Creator,
           Version,
           Source,
           Tags;
    int    BeatmapID,
        BeatmapSetID;

//Difficulty
    int CircleSize,
        ApproachRate,
        SliderTickRate;
    double SliderMultiplier,
           HPDrainRate,
           OverallDifficulty;

//Events
    string Background;

//Timing Points
    double TimingPoint[500000][9];
    int TimingPointCount; 

//Hit Objects
    int HitObject[500000][11];
    int HitObjectCount;
    
    osu()
    {
        AudioLeadIn=0;
        PreviewTime=0;
        Countdown=0;
        SampleSet="soft";
        StackLeniency=0.7;
        Mode=3;
        LetterboxInBreaks=0;
        SpecialStyle=0;
        WidescreenStoryboard=0;
        
        DistanceSpacing=2;
        BeatDivisor=4;
        GridSize=16;
        TimelineZoom=3.4999999;
        
        Creator="Converted by LiuRunky";
        Source="StepMania";
        BeatmapID=0;
        BeatmapSetID=-1;
        
        HPDrainRate=8;
        CircleSize=4;
        OverallDifficulty=8;
        ApproachRate=5;
        SliderMultiplier=1.4;
        SliderTickRate=1;
        
        TimingPointCount=0;
        HitObjectCount=0; 
    }
    
    void ReadinFormat()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
    }
    
    void PrintFormat()
    {
        fout<<"osu file format v14\n\n";
    }
    
    void ReadinGeneral()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        AudioFilename=getVal(str,0);
        
        getline(fin,str);
        AudioLeadIn=toint(getVal(str,0));
        
        getline(fin,str);
        PreviewTime=toint(getVal(str,0));
        
        getline(fin,str);
        Countdown=toint(getVal(str,0));
        
        getline(fin,str);
        SampleSet=getVal(str,0);
        
        getline(fin,str);
        StackLeniency=todouble(getVal(str,0));
        
        getline(fin,str);
        Mode=toint(getVal(str,0));
        
        getline(fin,str);
        LetterboxInBreaks=toint(getVal(str,0));
        
        getline(fin,str);
        SpecialStyle=toint(getVal(str,0));
        
        getline(fin,str);
        WidescreenStoryboard=toint(getVal(str,0));
        
        getline(fin,str);
    }
    
    void PrintGeneral()
    {
        fout<<"[General]\n";
        fout<<"AudioFilename: "<<AudioFilename<<'\n';
        fout<<"AudioLeadIn: "<<AudioLeadIn<<'\n';
        fout<<"PreviewTime: "<<PreviewTime<<'\n';
        fout<<"Countdown: "<<Countdown<<'\n';
        fout<<"SampleSet: "<<SampleSet<<'\n';
        fout<<"StackLeniency: "<<StackLeniency<<'\n';
        fout<<"Mode: "<<Mode<<'\n';
        fout<<"LetterboxInBreaks: "<<LetterboxInBreaks<<'\n';
        fout<<"SpecialStyle: "<<SpecialStyle<<'\n';
        fout<<"WidescreenStoryboard: "<<WidescreenStoryboard<<"\n\n";
    }
    
    void ReadinEditor()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        while(str.length()>1)
        {
            string Handle=getHandle(str);
            getline(fin,str);
        }
    }
    
    void PrintEditor()
    {
        fout<<"[Editor]\n";
        fout<<"DistanceSpacing: "<<DistanceSpacing<<'\n';
        fout<<"BeatDivisor: "<<BeatDivisor<<'\n';
        fout<<"GridSize: "<<GridSize<<'\n';
        fout<<"TimelineZoom: "<<fixed<<setprecision(7)<<TimelineZoom<<"\n\n";
    }
    
    void ReadinMetadata()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        Title=getVal(str,0);
        
        getline(fin,str);
        TitleUnicode=getVal(str,0);
        
        getline(fin,str);
        Artist=getVal(str,0);
        
        getline(fin,str);
        ArtistUnicode=getVal(str,0);
        
        getline(fin,str);
        Creator=getVal(str,0);
        
        getline(fin,str);
        Version=getVal(str,0);
        
        getline(fin,str);
        Source=getVal(str,0);
        
        getline(fin,str);
        Tags=getVal(str,0);
        
        getline(fin,str);
        BeatmapID=toint(getVal(str,0));
        
        getline(fin,str);
        BeatmapSetID=toint(getVal(str,0));
        
        getline(fin,str);
    }
    
    void PrintMetadata()
    {
        fout<<"[Metadata]\n";
        fout<<"Title:"<<Title<<'\n';
        fout<<"TitleUnicode:"<<TitleUnicode<<'\n';
        fout<<"Artist:"<<Artist<<'\n';
        fout<<"ArtistUnicode:"<<ArtistUnicode<<'\n';
        fout<<"Creator:"<<Creator<<'\n';
        fout<<"Version:"<<Version<<'\n';
        fout<<"Source:"<<Source<<'\n';
        fout<<"Tags:"<<Tags<<'\n';
        fout<<"BeatmapID:"<<BeatmapID<<'\n';
        fout<<"BeatmapSetID:"<<BeatmapSetID<<"\n\n";
    }
    
    void ReadinDifficulty()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        HPDrainRate=todouble(getVal(str,0));
        
        getline(fin,str);
        CircleSize=toint(getVal(str,0));
        
        getline(fin,str);
        OverallDifficulty=todouble(getVal(str,0));
        
        getline(fin,str);
        ApproachRate=toint(getVal(str,0));
        
        getline(fin,str);
        SliderMultiplier=todouble(getVal(str,0));
        
        getline(fin,str);
        SliderTickRate=toint(getVal(str,0));
        
        getline(fin,str);
    }
    
    void PrintDifficulty()
    {
        fout<<"[Difficulty]\n";
        fout<<"HPDrainRate:"<<HPDrainRate<<'\n';
        fout<<"CircleSize:"<<CircleSize<<'\n';
        fout<<"OverallDifficulty:"<<OverallDifficulty<<'\n';
        fout<<"ApproachRate:"<<ApproachRate<<'\n';
        fout<<"SliderMultiplier:"<<SliderMultiplier<<'\n';
        fout<<"SliderTickRate:"<<SliderTickRate<<"\n\n";
    }
    
    void ReadinEvents()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        getline(fin,str);
        {
            int i,j;
            for(i=0;i<str.length();i++)
                if(str[i]=='\"')
                    break;
            for(j=i+1;j<str.length();j++)
                if(str[j]=='\"')
                    break;
            i++,j--;
            
            Background=str.substr(i,j-i+1);
        }
        
        getline(fin,str);
        while(str.length()>0)
            getline(fin,str);
    }
    
    void PrintEvents()
    {
        fout<<"[Events]\n";
        fout<<"//Background and Video events\n";
        fout<<"0,0,\""<<Background<<"\",0,0\n";
        fout<<"//Break Periods\n//Storyboard Layer 0 (Background)\n//Storyboard Layer 1 (Fail)\n";
        fout<<"//Storyboard Layer 2 (Pass)\n//Storyboard Layer 3 (Foreground)\n//Storyboard Sound Samples\n\n";
    }
    
    void ReadinTimingPoints()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        while(str.length()>0)
        {
            TimingPointCount++;
            int cur=0,next;
            for(int i=1;i<=8;i++)
            {
                next=tonext(str,cur);
                string tmp=str.substr(cur,next-cur);
                cur=next+1;
                TimingPoint[TimingPointCount][i]=todouble(tmp);
            }
            getline(fin,str);
        }
        getline(fin,str);
    }
    
    void PrintTimingPoints()
    {
        fout<<"[TimingPoints]\n";
        for(int i=1;i<=TimingPointCount;i++,fout<<'\n')
            for(int j=1;j<=8;j++)
            {
                if(j==2)
                    if(TimingPoint[i][j]<0.0)
                        fout<<(int)TimingPoint[i][j];
                    else
                        fout<<fixed<<setprecision(7)<<TimingPoint[i][j];
                else
                    fout<<(int)TimingPoint[i][j];
                if(j<8)
                    fout<<',';
            }
        
        fout<<"\n\n";
    }
    
    void ReadinHitObjects()
    {
        string str;
        getline(fin,str);
        getline(fin,str);
        while((int)str.length()>0 && str[0]!=' ' && str[0]!='\n')
        {
            HitObjectCount++;
            int cur=0,next;
            for(int i=1;i<=10 && cur<str.length();i++)
            {
                next=tonext(str,cur);
                string tmp=str.substr(cur,next-cur);
                cur=next+1;
                
                HitObject[HitObjectCount][i]=toint(tmp);
            }
            
            getline(fin,str);
        }
    }
    
    void PrintHitObjects()
    {
        fout<<"[HitObjects]\n";
        for(int i=1;i<=HitObjectCount;i++)
        {
            for(int j=1;j<=5;j++)
                fout<<HitObject[i][j]<<',';
            
            for(int j=6;j<=(HitObject[i][6]==0?9:10);j++)
                fout<<HitObject[i][j]<<':';
            
            fout<<'\n';
        }
    }
    
    void Print()
    {
        fout.open(OutputFilename.c_str());
        
        PrintFormat();
        cout<<"Print format\n";
        PrintGeneral();
        cout<<"Print general\n";
        PrintEditor();
        cout<<"Print editor\n";
        PrintMetadata();
        cout<<"Print metadata\n";
        PrintDifficulty();
        cout<<"Print difficulty\n";
        PrintEvents();
        cout<<"Print events\n";
        PrintTimingPoints();
        cout<<"Print timing points\n";
        PrintHitObjects();
        cout<<"Print hit objects\n";
        
        fout.close();
        
        cout<<"Print Complete\n\n";
    }
    
    void Readin()
    {
        fin.open(InputFilename.c_str());
        
        ReadinFormat();
        cout<<"Readin format 0.0\n";
        ReadinGeneral();
        cout<<"Readin general 0.0\n";
        ReadinEditor();
        cout<<"Readin editor 0.0\n";
        ReadinMetadata();
        cout<<"Readin metadata 0.0\n";
        ReadinDifficulty();
        cout<<"Readin difficulty 0.0\n";
        ReadinEvents();
        cout<<"Readin events 0.0\n";
        ReadinTimingPoints();
        cout<<"Readin timing points 0.0\n";
        ReadinHitObjects();
        cout<<"Readin hit objects 0.0\n";
        
        fin.close();
        
        cout<<"Readin Complete\n\n";
    }
}OSUMANIA;

struct BLOCKUNIT
{
    int size;
    vector<int> v[5];
    
    BLOCKUNIT()
    {
        size=BLOCKSIZE;
        for(int i=1;i<=4;i++)
            for(int j=1;j<=size;j++)
                v[i].push_back(0);
    }
};

struct sm
{
    string Title,
           Subtitle,
           Artist,
           TitleTranslit,
           SubtitleTranslit,
           ArtistTranslit,
           Genre,
           Credit,
           Music,
           Background,
           Level,
           Description,
           Selectable;
    int BlockCount,
        BpmCount;
    double Offset,
           SampleStart,
           SampleLength,
           Bpm[5005][3];
    BLOCKUNIT Block[1005];
    
    sm()
    {
        Credit="Osu Mania";
        Description="Hard";
        Level="0";
        Selectable="YES";
        
        BlockCount=0;
        BpmCount=0;
        
        Offset=0.0;
        SampleStart=0.0;
        SampleLength=30.0;
    }
    
    void AddBpm(string str)
    {
        int cur=0,next;
        while(cur<str.length())
        {
            BpmCount++;
            
            next=tonext(str,cur);
            
            int i;
            for(i=cur;;i++)
                if(str[i]=='=')
                    break;
            
            Bpm[BpmCount][1]=todouble(str.substr(cur,i-cur));
            Bpm[BpmCount][2]=todouble(str.substr(i+1,next-(i+1)));
            
            cur=next+1;
        }
    }
    
    void ReadinBasicInformation()
    {
        string str;
        getline(fin,str);
        
        while(str[0]!='/')
        {
            if((int)str.length()==0)
            {
                getline(fin,str);
                continue;
            }
            
            string tmp;
            
            getline(fin,tmp);
            while(tmp[0]!='#' && tmp[0]!='/')
            {
                str=str+tmp;
                getline(fin,tmp);
            }
            
            if(getIdx(str)=="TITLE")
                Title=getVal(str,1);
            if(getIdx(str)=="SUBTITLE")
                Subtitle=getVal(str,1);
            if(getIdx(str)=="ARTIST")
                Artist=getVal(str,1);
            if(getIdx(str)=="TITLETRANSLIT")
                TitleTranslit=getVal(str,1);
            if(getIdx(str)=="SUBTITLETRANSLIT")
                SubtitleTranslit=getVal(str,1);
            if(getIdx(str)=="ARTISTTRANSLIT")
                ArtistTranslit=getVal(str,1);
            if(getIdx(str)=="GENRE")
                Genre=getVal(str,1);
            
            if(getIdx(str)=="CREDIT")
                Credit=getVal(str,1);
            if(getIdx(str)=="BACKGROUND")
                Background=getVal(str,1);
            if(getIdx(str)=="MUSIC")
                Music=getVal(str,1);
            
            if(getIdx(str)=="OFFSET")
                Offset=todouble(getVal(str,1));
            if(getIdx(str)=="SAMPLESTART")
                SampleStart=todouble(getVal(str,1));
            if(getIdx(str)=="SAMPLELENGTH")
                SampleLength=todouble(getVal(str,1));
            if(getIdx(str)=="SELECTABLE")
                Selectable=getVal(str,1);
            
            if(getIdx(str)=="BPMS")
                AddBpm(getVal(str,1));
             
            str=tmp;
        }
    }
    
    void PrintBasicInformation()
    {
        fout<<"#TITLE:"<<Title<<";\n";
        fout<<"#SUBTITLE:"<<Subtitle<<";\n";
        fout<<"#ARTIST:"<<Artist<<";\n";
        fout<<"#TITLETRANSLIT:"<<TitleTranslit<<";\n";
        fout<<"#SUBTUTLETRANSLIT:"<<SubtitleTranslit<<";\n";
        fout<<"#ARTISTTRANSLIT:"<<ArtistTranslit<<";\n";
        fout<<"#GENRE:"<<Genre<<";\n";
        
        fout<<"#CREDIT:"<<Credit<<";\n";
        fout<<"#BACKGROUND:"<<Background<<";\n";
        fout<<"#MUSIC:"<<Music<<";\n";
        
        fout<<"#OFFSET:"<<fixed<<setprecision(5)<<Offset<<";\n";
        fout<<"#SAMPLESTART:"<<SampleStart<<";\n";
        fout<<"#SAMPLELENGTH:"<<SampleLength<<";\n";
        fout<<"#BPMS:";
        for(int i=1;i<=BpmCount;i++)
        {
            fout<<Bpm[i][1]<<"="<<Bpm[i][2];
            
            if(i!=BpmCount)
                fout<<",\n";
        }
        fout<<";\n";
    }
    
    void ReadinDifficulties()
    {
        string str;
        int i;
        
        getline(fin,str);
        getline(fin,str);
        getline(fin,str);
        
        getline(fin,str);
        for(i=0;i<str.length();i++)
            if(str[i]!=' ')
                break;
        Description=str.substr(i,(int)str.length()-i-1);
        
        getline(fin,str);
        for(i=0;i<str.length();i++)
            if(str[i]!=' ')
                break;
        Level=str.substr(i,(int)str.length()-i-1);
        
        getline(fin,str);
    }
    
    void PrintDifficulties()
    {
        fout<<"//---------------dance-single - ----------------\n";
        fout<<"#NOTES:\n";
        fout<<"     dance-single:\n";
        fout<<"     :\n";
        fout<<"     "<<Description<<":\n";
        fout<<"     "<<Level<<":\n";
        fout<<"     0.000,0.000,0.000,0.000,0.000:\n";
    }
    
    void ReadinObjects()
    {
        string str;
        BLOCKUNIT tmp;
        getline(fin,str);
        
        while(str[0]!=';')
        {
            if(str[0]=='/' || str[0]==' ')
            {
                getline(fin,str);
                continue;
            }
            if(str[0]==',')
            {
                BlockCount++;
                Block[BlockCount]=tmp;
                
                getline(fin,str);
                continue;
            }
            
            tmp.size++;
            for(int i=0;i<str.length();i++)
                tmp.v[i+1].push_back(int(str[i]-'0'));
            getline(fin,str);
        }
        
        BlockCount++;
        Block[BlockCount]=tmp;
    }
    
    void PrintObjects()
    {
        for(int i=1;i<=BlockCount;i++)
        {
            fout<<"  //Measure "<<i<<'\n';
            for(int j=0;j<Block[i].size;j++,fout<<'\n')
                for(int k=1;k<=4;k++)
                    fout<<Block[i].v[k][j];
            if(i!=BlockCount)
                fout<<",";
        }
        fout<<";\n";
    }
    
    void Print()
    {
        fout.open(OutputFilename.c_str());
        PrintBasicInformation();
        cout<<"Print basic information\n";
        PrintDifficulties();
        cout<<"Print difficulties\n";
        PrintObjects();
        cout<<"Print objects\n";
        fout.close();
        cout<<"Print Complete\n\n";
    }
    
/*    void Readin()
    {
        fin.open(InputFilename.c_str());
        
        ReadinBasicInformation();
        cout<<"Readin basic information 0.0\n";
        ReadinDifficulties();
        cout<<"Readin difficulties 0.0\n";
        ReadinObjects();
        cout<<"Readin objects 0.0\n";
        
        fin.close();
        
        cout<<"Readin Complete\n\n";
    }*/
}STEPMANIA;

void toSM()
{
    STEPMANIA.Title=OSUMANIA.Title;
    STEPMANIA.Subtitle=OSUMANIA.Source;
    STEPMANIA.Artist=OSUMANIA.Artist;
    STEPMANIA.TitleTranslit=OSUMANIA.TitleUnicode;
    STEPMANIA.SubtitleTranslit=OSUMANIA.Source;
    STEPMANIA.ArtistTranslit=OSUMANIA.ArtistUnicode;
    
    STEPMANIA.Credit=OSUMANIA.Creator;
    STEPMANIA.Background=OSUMANIA.Background;
    STEPMANIA.Music=OSUMANIA.AudioFilename;
    
    STEPMANIA.Offset=-OSUMANIA.TimingPoint[1][1]/(double)1000;
    STEPMANIA.SampleStart=OSUMANIA.PreviewTime/(double)1000;
    STEPMANIA.SampleLength=15.0;
    
    STEPMANIA.Description="Hard";
    string LevelStr;
    for(int i=0;i<OSUMANIA.Version.length();i++)
        if(OSUMANIA.Version[i]>='0' && OSUMANIA.Version[i]<='9')
            LevelStr=LevelStr+OSUMANIA.Version[i];
    if(LevelStr.length()==0)
        LevelStr="0";
    STEPMANIA.Level=LevelStr;
    
    STEPMANIA.BpmCount=1;
    STEPMANIA.Bpm[1][1]=0.000;
    vector<double> BpmPeriod,BpmTimeCount;
    int LastTime,LastIndex=0,Pointer=1;
    while(OSUMANIA.TimingPoint[Pointer][2]<0)
        Pointer++;
    LastTime=OSUMANIA.TimingPoint[Pointer][1];
    BpmPeriod.push_back(OSUMANIA.TimingPoint[Pointer][2]);
    BpmTimeCount.push_back(0);
    for(int i=Pointer+1;i<=OSUMANIA.TimingPointCount;i++)
    {
        if(OSUMANIA.TimingPoint[i][2]<0)
            continue;
        
        bool AddNewPeriod=true;
        for(int j=0;j<BpmPeriod.size();j++)
            if(BpmPeriod[j]==OSUMANIA.TimingPoint[i][2])
                AddNewPeriod=false;
        if(AddNewPeriod)
        {
            BpmPeriod.push_back(OSUMANIA.TimingPoint[i][2]);
            BpmTimeCount.push_back(0);
        }
        BpmTimeCount[LastIndex]+=(OSUMANIA.TimingPoint[i][1]-LastTime);
        LastTime=OSUMANIA.TimingPoint[i][1];
        for(int j=0;j<BpmPeriod.size();j++)
            if(BpmPeriod[j]==OSUMANIA.TimingPoint[i][2])
            {
                LastIndex=j;
                break;
            }
    }
    BpmTimeCount[LastIndex]+=(OSUMANIA.HitObject[OSUMANIA.HitObjectCount][3]-LastTime);
    
    int MaximumLength=0,MaximumLengthBpm=0;
    for(int i=0;i<BpmTimeCount.size();i++)
        if(BpmTimeCount[i]>MaximumLength)
        {
            MaximumLength=BpmTimeCount[i];
            MaximumLengthBpm=i;
        }
    STEPMANIA.Bpm[1][2]=60000/(double)BpmPeriod[MaximumLengthBpm];
    
    int StartingPoint=OSUMANIA.TimingPoint[1][1];
    int Period=OSUMANIA.TimingPoint[1][2]*4;
    int MaximumTime=0;
    for(int i=1;i<=OSUMANIA.HitObjectCount;i++)
    {
        MaximumTime=max(MaximumTime,OSUMANIA.HitObject[i][3]);
        MaximumTime=max(MaximumTime,OSUMANIA.HitObject[i][6]);
    }
    
    STEPMANIA.BlockCount=(MaximumTime-StartingPoint+Period)/Period+1;
    cout<<"BlockCount="<<STEPMANIA.BlockCount<<endl;
    
    for(int i=1;i<=OSUMANIA.HitObjectCount;i++)
        if(OSUMANIA.HitObject[i][6]==0)//Single Note
        {
            int BlockIndex=(OSUMANIA.HitObject[i][3]-StartingPoint)/Period;
            int Reminder=OSUMANIA.HitObject[i][3]-StartingPoint-BlockIndex*Period;
            BlockIndex++;
            
            int NotePlace=(OSUMANIA.HitObject[i][1]/64+1)/2;
            int PlaceinBlock=int(1.0*BLOCKSIZE*Reminder/(double)Period);
            STEPMANIA.Block[BlockIndex].v[NotePlace][PlaceinBlock]=1;
        }
        else//Long Note
        {
            int BlockIndex=(OSUMANIA.HitObject[i][3]-StartingPoint)/Period;
            int Reminder=OSUMANIA.HitObject[i][3]-StartingPoint-BlockIndex*Period;
            BlockIndex++;
            
            int NotePlace=(OSUMANIA.HitObject[i][1]/64+1)/2;
            int PlaceinBlock=int(1.0*BLOCKSIZE*Reminder/(double)Period);
            STEPMANIA.Block[BlockIndex].v[NotePlace][PlaceinBlock]=2;
            
            BlockIndex=(OSUMANIA.HitObject[i][6]-StartingPoint)/Period;
            Reminder=OSUMANIA.HitObject[i][6]-StartingPoint-BlockIndex*Period;
            BlockIndex++;
            
            PlaceinBlock=int(1.0*BLOCKSIZE*Reminder/(double)Period);
            STEPMANIA.Block[BlockIndex].v[NotePlace][PlaceinBlock]=3;
        }
}

int main()
{
    fin.open("input.txt");
    getline(fin,InputFilename);
    getline(fin,OutputFilename);
    fin.close();
    
    OSUMANIA.Readin();
    toSM();
    STEPMANIA.Print();
    
    return 0;
}
