package com.devyy.openyspider.integration.leetcode.gson;

import java.util.List;

/**
 * @since 2019-02-06
 */
public class GetQuestionTranslationGson {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<TranslationsBean> translations;

        public List<TranslationsBean> getTranslations() {
            return translations;
        }

        public void setTranslations(List<TranslationsBean> translations) {
            this.translations = translations;
        }

        public static class TranslationsBean {
            /**
             * questionId : 1
             * title : 两数之和
             * __typename : AppliedTranslationNode
             */

            private String questionId;
            private String title;
            private String __typename;

            public String getQuestionId() {
                return questionId;
            }

            public void setQuestionId(String questionId) {
                this.questionId = questionId;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String get__typename() {
                return __typename;
            }

            public void set__typename(String __typename) {
                this.__typename = __typename;
            }
        }
    }
}
