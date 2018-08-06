package com.udacity.adibella.whatsinmyfridge.util;

import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;

import org.xml.sax.XMLReader;

import java.util.Stack;

import timber.log.Timber;

public class ListTagHandler implements Html.TagHandler {
    private static final String OL_TAG = "ol";
    private static final String UL_TAG = "ul";
    private static final String LI_TAG = "li";
    private static final int INDENT_PX = 10;
    private static final int LIST_ITEM_INDENT_PX = INDENT_PX * 2;
    private static final BulletSpan BULLET_SPAN = new BulletSpan(INDENT_PX);

    private final Stack<ListTag> lists = new Stack<ListTag>();

    @Override
    public void handleTag(final boolean opening, final String tag, final Editable output, final XMLReader xmlReader)
    {
        if (UL_TAG.equalsIgnoreCase(tag))
        {
            if (opening)
            {   // handle <ul>
                lists.push(new Ul());
            }
            else
            {   // handle </ul>
                lists.pop();
            }
        }
        else if (OL_TAG.equalsIgnoreCase(tag))
        {
            if (opening)
            {   // handle <ol>
                lists.push(new Ol()); // use default start index of 1
            }
            else
            {   // handle </ol>
                lists.pop();
            }
        }
        else if (LI_TAG.equalsIgnoreCase(tag))
        {
            if (opening)
            {   // handle <li>
                lists.peek().openItem(output);
            }
            else
            {   // handle </li>
                lists.peek().closeItem(output, lists.size());
            }
        }
        else
        {
            Timber.d("Found an unsupported tag " + tag);
        }
    }

    private abstract static class ListTag
    {
        public void openItem(final Editable text)
        {
            if (text.length() > 0 && text.charAt(text.length() - 1) != '\n')
            {
                text.append("\n");
            }
            final int len = text.length();
            text.setSpan(this, len, len, Spanned.SPAN_MARK_MARK);
        }

        public final void closeItem(final Editable text, final int indentation)
        {
            if (text.length() > 0 && text.charAt(text.length() - 1) != '\n')
            {
                text.append("\n");
            }
            final Object[] replaces = getReplaces(text, indentation);
            final int len = text.length();
            final ListTag listTag = getLast(text);
            final int where = text.getSpanStart(listTag);
            text.removeSpan(listTag);
            if (where != len)
            {
                for (Object replace : replaces)
                {
                    text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        protected abstract Object[] getReplaces(final Editable text, final int indentation);

        private ListTag getLast(final Spanned text)
        {
            final ListTag[] listTags = text.getSpans(0, text.length(), ListTag.class);
            if (listTags.length == 0)
            {
                return null;
            }
            return listTags[listTags.length - 1];
        }
    }

    private static class Ul extends ListTag
    {

        @Override
        protected Object[] getReplaces(final Editable text, final int indentation)
        {
            // Nested BulletSpans increases distance between BULLET_SPAN and text, so we must prevent it.
            int bulletMargin = INDENT_PX;
            if (indentation > 1)
            {
                bulletMargin = INDENT_PX - BULLET_SPAN.getLeadingMargin(true);
                if (indentation > 2)
                {
                    // This get's more complicated when we add a LeadingMarginSpan into the same line:
                    // we have also counter it's effect to BulletSpan
                    bulletMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX;
                }
            }
            return new Object[] {
                    new LeadingMarginSpan.Standard(LIST_ITEM_INDENT_PX * (indentation - 1)),
                    new BulletSpan(bulletMargin)
            };
        }
    }

    private static class Ol extends ListTag
    {
        private int nextIdx;

        public Ol()
        {
            this(1); // default start index
        }

        public Ol(final int startIdx)
        {
            this.nextIdx = startIdx;
        }

        @Override
        public void openItem(final Editable text)
        {
            super.openItem(text);
            text.append(Integer.toString(nextIdx++)).append(". ");
        }

        @Override
        protected Object[] getReplaces(final Editable text, final int indentation)
        {
            int numberMargin = LIST_ITEM_INDENT_PX * (indentation - 1);
            if (indentation > 2)
            {
                // Same as in ordered lists: counter the effect of nested Spans
                numberMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX;
            }
            return new Object[] { new LeadingMarginSpan.Standard(numberMargin) };
        }
    }

}
