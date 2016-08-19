package com.intellij.openapi.vcs.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionDescription;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AnnotatePreviousRevisionAction extends AnnotateRevisionAction {
  @Nullable private final List<VcsRevisionDescription> myRevisions;
  @Nullable private final VcsRevisionDescription myLastRevision;

  public AnnotatePreviousRevisionAction(@NotNull FileAnnotation annotation, @NotNull AbstractVcs vcs) {
    super("Annotate Previous Revision", "Annotate successor of selected revision in new tab", AllIcons.Actions.Annotate,
          annotation, vcs);
    List<? extends VcsRevisionDescription> revisions = annotation.getRevisionDescriptions();
    if (revisions == null) {
      myRevisions = null;
      myLastRevision = null;
      return;
    }

    Map<VcsRevisionNumber, VcsRevisionDescription> map = new HashMap<>();
    for (int i = 0; i < revisions.size(); i++) {
      VcsRevisionDescription revision = revisions.get(i);
      VcsRevisionDescription previousRevision = i + 1 < revisions.size() ? revisions.get(i + 1) : null;
      map.put(revision.getRevisionNumber(), previousRevision);
    }

    myRevisions = new ArrayList<>(annotation.getLineCount());
    for (int i = 0; i < annotation.getLineCount(); i++) {
      myRevisions.add(map.get(annotation.getLineRevisionNumber(i)));
    }

    myLastRevision = ContainerUtil.getFirstItem(revisions);
  }

  @Override
  @Nullable
  public List<VcsRevisionDescription> getRevisions() {
    return myRevisions;
  }

  @Nullable
  @Override
  protected VcsFileRevision getFileRevision(@NotNull AnActionEvent e) {
    if (getCurrentLine() == UpToDateLineNumberProvider.ABSENT_LINE_NUMBER) {
      return myAnnotation.getRevisionByDescription(myLastRevision);
    }
    return super.getFileRevision(e);
  }
}
