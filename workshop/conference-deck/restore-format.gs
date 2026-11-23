/** @OnlyCurrentDoc */
function restoreFormat(){
 const p=SlidesApp.getActivePresentation();if(p.getId()!=='13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4')throw new Error('Wrong deck');
 const titles=['','What is an LLM?','Why Offline?','What Can We Rely On?','Tokens & Context','Weights & Quantization','The Mobile AI Landscape','Choosing a Model','From MediaPipe to LiteRT-LM','Kotlin Multiplatform','Shared Inference API','FunctionGemma','Defining a Tool','Function Calling','Function Calling on iOS','The System Prompt','Validating a Tool Call','Demo Time','Before You Ship','Resources'];
 p.getSlides().forEach((s,i)=>{if(!i)return;
 s.getLines().filter(l=>Math.abs(l.getTop()-94)<2&&l.getWidth()>500).forEach(l=>l.remove());
 s.getShapes().filter(a=>a.getTop()>=374&&/^\d{2}\s*$/.test(a.getText().asString())).forEach(a=>a.remove());
 const title=s.getShapes().find(a=>a.getTop()===34);if(title){title.setLeft(35).setTop(38).setWidth(650).setHeight(65);title.getText().setText(titles[i]);title.getText().getTextStyle().setFontFamily('Google Sans').setFontSize(38).setBold(false).setForegroundColor('#FFFFFF');title.getText().getParagraphStyle().setParagraphAlignment(SlidesApp.ParagraphAlignment.CENTER);}
 if([10,13,15,16].includes(i)){
 const panel=s.getShapes().find(a=>!a.getText().asString().trim()&&a.getWidth()>=590&&a.getTop()>100);
 if(panel){panel.getFill().setSolidFill('#F5F6F7');panel.setLeft(25).setWidth(670);}
 s.getShapes().filter(a=>a.getText().asString().trim()&&a.getTop()>120&&a.getText().getTextStyle().getFontFamily()==='Roboto Mono').forEach(a=>{if(panel&&a.getTop()<panel.getTop()+panel.getHeight()){a.setLeft(45).setWidth(630);a.getText().getTextStyle().setForegroundColor('#163442').setFontSize(i===13?23:17);}});
 }
 });
 console.log('Original-style format applied to 20 slides');
}
