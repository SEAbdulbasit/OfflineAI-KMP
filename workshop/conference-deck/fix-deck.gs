/** @OnlyCurrentDoc */
function fixConference() {
 const p=SlidesApp.getActivePresentation();
 if(p.getId()!=='13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4') throw new Error('Wrong deck');
 const ss=p.getSlides();
 ss[4].replaceAllText('CPU 33 / GPU 24 tok/s','CPU 33 / GPU 24 decode tok/s');
 ss[15].replaceAllText('System models / Android + Apple','Android system models');
 const notes=ss[3].getNotesPage().getSpeakerNotesShape().getText();
 notes.appendText('\nTool capabilities depend on the selected API and model.');
 const t=ss[15].insertTextBox('Apple Foundation Models',285,321,385,25);
 t.getText().getTextStyle().setFontFamily('Google Sans').setFontSize(17).setBold(false).setForegroundColor('#F4F7F5');
 t.getText().getParagraphStyle().setSpaceAbove(0).setSpaceBelow(0);
 t.setLinkUrl('https://developer.apple.com/documentation/foundationmodels');
 console.log('Critique fixes applied');
 auditConference();
}
function auditConference() {
  const p=SlidesApp.getActivePresentation();
  console.log(JSON.stringify({count:p.getSlides().length,width:p.getPageWidth(),height:p.getPageHeight()}));
  p.getSlides().forEach((s,i)=>{
    const errors=[];
    s.getPageElements().forEach(e=>{if(e.getLeft() < -1 || e.getTop() < -1 || e.getLeft()+e.getWidth()>721 || e.getTop()+e.getHeight()>406) errors.push(e.getObjectId());});
    console.log(JSON.stringify({slide:i+1,id:s.getObjectId(),images:s.getImages().length,notes:s.getNotesPage().getSpeakerNotesShape().getText().asString().length,offCanvas:errors,text:s.getShapes().map(x=>x.getText().asString()).join(' / ')}));
  });
}
