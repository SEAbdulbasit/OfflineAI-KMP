/** @OnlyCurrentDoc */
function qualifyCapabilities(){
 const p=SlidesApp.getActivePresentation();if(p.getId()!=='13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4')throw new Error('Wrong deck');
 const s=p.getSlides();if(s.length!==18)throw new Error('Expected 18 slides');
 const replacements=[
 [3,'Useful AI starts with everyday tasks','Start with narrow, testable tasks'],
 [3,'Meeting notes','Short notes'],[3,'Action items','Draft action items'],[3,'Bundled documents','FAQ excerpt'],[3,'Answers + quotes','Candidate answer'],[3,'User request','Allowed request'],[3,'Device action','Proposed tool call'],
 [16,'Start with one task you can evaluate','Small models need narrow tasks'],
 [16,'Meeting notes → action items','Short notes → draft action items'],
 [16,'Bundled FAQ → answer + source quote','Supplied excerpt → candidate answer'],
 [16,'Request → validated device action','Allowed request → proposed tool call'],
 [16,'Correctness / latency / peak RAM','Evaluate errors / latency / peak RAM'],
 [15,'Useful output, then a real action','Test the output before taking action'],
 [15,'Notes → action items','Notes → draft items']];
 replacements.forEach(([i,a,b])=>s[i].replaceAllText(a,b));
 const caveat=s[3].insertTextBox('Examples to evaluate—not guaranteed capabilities',60,350,600,25);
 caveat.getBorder().setTransparent();caveat.getText().getTextStyle().setFontFamily('Google Sans').setFontSize(17).setBold(false).setForegroundColor('#B7DBAD');
 const n=s[3].getNotesPage().getSpeakerNotesShape().getText();
 n.setText('0:50 · 2:00–2:50\n\nThese are candidate product tasks, not claims of reliable performance from every mobile model. Start with short inputs and a small tool set: models may miss action items, invent an answer, or emit the wrong tool or arguments.\n\nOffline describes execution location, not intelligence or quality. Accuracy depends on the specific model, training, quantization, context and task. The project implements these flows, but the deck does not establish their reliability with a measured evaluation. For FAQ use, supply the relevant excerpt and verify the answer against it; an included quote is not proof of correctness. Validate tool calls in the app and test requests where no action is appropriate.\n\n[Conference edition 2026-10-22]');
 s[16].getNotesPage().getSpeakerNotesShape().getText().appendText('\n\nThese examples are evaluation targets, not established model capabilities. Test factual mistakes, omissions, unsupported requests and malformed tool calls; retain deterministic logic for tasks that do not need a language model. Offline does not inherently make a model weak, but mobile resource budgets often constrain model capacity and context.');
 console.log('Capability wording qualified; '+s.length+' slides');
}
