[종합설계] 안드로이드 실시간 웹뷰 번역 어플리케이션
======================

# 1. 앱 소개
## 1.1. 앱 설명
1. 모바일 환경에서 실시간으로 브라우저 내 문장이나 단어의 번역을 웹뷰를 이용하여 제공하는 시스템입니다.
2. 단어 및 문장을 드래그하면 해당 단어의 사전적 의미를 웹뷰 화면에 그대로 표시합니다.
3. 번역 결과를 사용자의 설정에 맞게 가독성있게 표현합니다.
4. 문서 번역 기능을 이용하여 PDF나 이미지의 텍스트를 번역할 수 있습니다.
    
## 1.2. 기능
1. 웹 뷰 실시간 번역
2. 문서 번역
3. PDF 번역
    
## 1.3. 사용한 툴
![제목 없음](https://user-images.githubusercontent.com/95574210/187355845-0f44318f-027d-4ab2-a406-79b17d68cfd1.png)

## 1.4. 사용 안내
### 1.4.1. 웹 뷰 실시간 번역
![그림2](https://user-images.githubusercontent.com/95574210/187357016-f6c8e372-8a69-438c-9d6f-bf0c9c227c80.png)
<br><br/>웹 페이지 번역

![그림1](https://user-images.githubusercontent.com/95574210/187357085-548e3566-1568-47dc-84fb-d7803ef1c1bf.png)
<br><br/>웹 페이지 스크롤 번역

### 1.4.2. 이미지 및 PDF 번역

![그림3](https://user-images.githubusercontent.com/95574210/187357159-167ca2f0-a877-4759-a9c8-2e3fc9c339ec.png)

****
# 2. 어플 사용법
## 2.1. 동작 순서도
### 2.1.1. 웹 뷰 실시간 번역
    1. 어플을 키고, 좌상 메뉴에서 웹 사이트 번역을 선택한다.
    2. 번역이 필요한 사이트에 들어간다.
    3.1. 단어 및 문장 번역이 필요한 경우, 해당 단어, 문장을 드래그한다. 그 후, 생성된 탭에서 번역을 터치한다.
    3.2. 사이트 전문 번역이 필요한 경우, 아무 단어, 문장을 드래그한다. 그 후, 생성된 탭에서 전체번역을 터치한다.
 <br><br/>
    
 ![그림4](https://user-images.githubusercontent.com/95574210/187357564-88d0017f-4741-4ecb-885a-ee53c4e43939.png)
 <br><br/>
 ![그림5](https://user-images.githubusercontent.com/95574210/187357572-fd842637-d3b3-4a24-a0f8-1ce798552617.png)
 <br><br/>
 ![그림6](https://user-images.githubusercontent.com/95574210/187357578-17e14e2a-ae5d-4a44-a580-9a235b08c55b.png)
 <br><br/>

### 2.1.2 이미지 번역
    1. 어플을 키고, 좌상 메뉴에서 문서 번역을 선택한다.
    2. 이미지 번역을 선택한다.
    3. 파일 액세스를 허용한다.
    4. 원하는 이미지를 선택한다.
    5. 번역 버튼을 누른다.
<br><br/>
![그림7](https://user-images.githubusercontent.com/95574210/187357583-e240b829-2231-45d8-b6c8-0e10156f67bb.png)
<br><br/>
![그림8](https://user-images.githubusercontent.com/95574210/187357589-b863952b-cf96-4301-bf89-f6ca60979e23.png)  
<br><br/>
![그림9](https://user-images.githubusercontent.com/95574210/187357592-daff43f7-c05f-4fe7-ae22-ee748568ed31.png)  
<br><br/> 

### 2.1.3 PDF 번역
    1. 어플을 키고, 좌상 메뉴에서 문서 번역을 선택한다.
    2. PDF 번역을 선택한다.
    3. 파일 액세스를 허용한다.
    4. 원하는 파일를 선택한다.
    5. 번역 버튼을 누른다.

<br><br/>
![그림10](https://user-images.githubusercontent.com/95574210/187357597-24c29d87-1527-476f-b49a-ccd828b6ba10.png)
<br><br/>
![캡처](https://user-images.githubusercontent.com/95574210/187357600-9fdde3b2-65ea-47bf-ba6a-ebd4c0c364fd.PNG)
<br><br/>

## 2.2. 동작 설계도
### 2.2.1. 웹 뷰 실시간 번역
    1. 번역 구간을 설정한다.
    2. 구간을 설정하면 해당 구간의 단어를 파파고와 연동한다.
    3. 연동한 후, 번역의 결과를 구간 바로 다음 문장으로 띄우거나, 덧씌운다.
    4. 전체 번역의 경우 화면 전체를 인식하게 한다.
    5. 그 후, 스크롤을 내리면 해당 화면을 실시간으로 인식하여 번역 내용을 덧씌운다.

### 2.2.2. 이미지 번역
    1. 이미지의 경우, 한장의 이미지를 번역한다.
    2. 고로 화면을 한번 인식하는 것으로 문자를 인식하여 번역한다.
    3. 그 후, 인식한 문자를 번역하여 내용을 덧씌운다.

### 2.2.3. PDF 번역
    1. PDF의 경우, 페이지를 기준으로 번역한다.
    2. 매 페이지가 넘어갈 때 마다, 재인식을 실시한다.
    3. 그 후, 인식한 문자를 번역하여 내용을 덧씌운다.

****
# 3. 프로젝트 기능 설명
## 3.1. 웹 뷰 실시간 번역
    웹 뷰 번역의 경우, 번역 어플을 사용 시에 필연적으로 겪는 어플의 이동을 줄이고,
    실시간으로 뒤 또는 덧씌우는 방식으로 진행하여, 사소한 시간의 낭비조차 절약한다.
    또한, 전체적인 번역만이 아닌, 구간 구간 번역 범위를 직접 지정하므로써, 번역 이외에도 사용처로서 기능한다.
    
## 3.2. 이미지 번역
     이미지 번역의 경우, 단순 웹 뷰의 번역만을 두지 않고, 차별화를 두기 위한 기능중 하나로, 
     이미지의 번역또한 화면 인식을 이용한 실시간 번역을 사용한다.

## 3.3. PDF 번역
    PDF 번역의 경우, 단순 웹 뷰의 번역만을 두지 않고, 차별화를 두기 위한 기능중 하나로,
    PDF의 번역또한 화면 인식을 이용한 실시간 번역을 사용한다. 

****
# 4. 버그

****
# 5. 프로그램 작성자 및 도움을 준 분
## 제작자
    김재혁
    김다훈 hun062@gmail.com
    이건영
## 도우미
    

****
# 6. 버전
    0.3.0

***** 
## ○ 참고문서
* 구글 번역 API(<http://mashable.com/2013/06/24/markdown-tools/>)
* 파파고 API(<https://guide.ncloud-docs.com/docs/naveropenapiv3-translation-nmt>)
* PDF API(<https://github.com/TomRoush/PdfBox-Android>, 
          <https://github.com/ViliusSutkus89/pdf2htmlEX-Android>)
* 플루토(<https://ko.flitto.com/business/api-solution>)
* CSS(<https://www.w3schools.com/cssref/default.asp>)
* 구글 머테리얼 디자인(<https://fonts.google.com/icons?selected=Material+Icons>)
