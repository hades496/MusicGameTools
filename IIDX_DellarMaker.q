SetSimMode 1
SK = "Num +"
PW = Array("Num 4", "Num 9", "Num 6", "Num 6")
P1 = "z"
Start = "Enter"
VEFX = "9"
Effect = "0"
SP = "Up"
StepName = Array("01主界面", "02刷卡", "03刷卡完毕", "04选择SP", "05选择模式", "06时间确认", "07选歌界面", "08退出确认", "09结束")

Sub PressKeyList(StepNum)
	Delay 1000
	Select Case StepNum
		Case 0
			KeyDown SK, 1
			Delay 50
			KeyUp SK, 1
		Case 1
			Delay 1000
			For Si = 0 To 3
				KeyDown PW(Si), 1
				Delay 50
				KeyUp PW(Si), 1
				Delay 50
			Next
		Case 2, 3
			KeyDown Start, 1
			Delay 50
			KeyUp Start, 1
		Case 4, 5
			KeyDown P1, 1
			Delay 50
			KeyUp P1, 1
		Case 6
			Delay 1000
			KeyDown VEFX, 1
			Delay 20
			KeyDown Effect, 1
			Delay 20
			KeyUp VEFX, 1
			Delay 20
			KeyUp Effect, 1
		Case 7
			KeyDown SP, 1
			Delay 50
			KeyUp SP, 1
			Delay 200
			KeyDown P1, 1
			Delay 50
			KeyUp P1, 1
		Case 8
			KeyPress P1, 1
	End Select
End Sub

Sub PutToList (str)
	form1.ListBox1.AddItem str
	Call Plugin.Bkgnd.KeyPress(Form1.ListBox1.Hwnd, 40)
End Sub


Function UntilFindAndPress(StepNum)
	intX = - 1 
	intY = - 1 
	PutToList "当前步骤" + Cstr(StepNum+1)
	PutToList "开始寻找" + StepName(StepNum)
	times = 0
	Do Until intX > 0 and intY > 0
		If times > 200 Then 
			UntilFindAndPress = True
			Exit Function
		End If
		times = times + 1		
		If (StepNum = 1) Then 
			PressKeyList(8)
			PressKeyList(0)
		End If
		'PutToList "开始第" + Cstr(times) + "次寻找" + "Attachment:\" + StepName(StepNum) + ".bmp"
		Delay 1000
		FindPic 0, 0, 1280, 720, "Attachment:\" + StepName(StepNum) + ".bmp", 0.8, intX, intY
	Loop
	PutToList "找到"+StepName(StepNum)
	PressKeyList (StepNum)
	UntilFindAndPress = False
End Function


PutToList "脚本开始："
Total = 0
While (1)
	For i = 1 To 8 Step 1
		If  UntilFindAndPress(i) Then 
			Exit For
		End If		
	Next
	Total = Total + 1
	Form1.Label3.Caption = "当前游戏次数："+ Cstr(Total)+"次"
Wend

Event Form1.Label2.Click
	Form1.Label2.Caption = "在游戏处在未有玩状态时，按下方的开始按钮或者快捷键F10即可启动脚本"
End Event
