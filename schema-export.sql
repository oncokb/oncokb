
    alter table alteration 
        drop 
        foreign key FKD5A6189FC602BC92;

    alter table alteration 
        drop 
        foreign key FKD5A6189FAEE285DC;

    alter table alteration_reference_genome 
        drop 
        foreign key FKE8AFDC0BAAD5975;

    alter table cancer_type 
        drop 
        foreign key FKAC902999B4A9E9F8;

    alter table cancer_type_child 
        drop 
        foreign key FK25672AF690C0BC52;

    alter table cancer_type_child 
        drop 
        foreign key FK25672AF64C5916CF;

    alter table cancer_type_synonym 
        drop 
        foreign key FKD3F663554C5916CF;

    alter table drug_family 
        drop 
        foreign key FKD5F254E34E48DF2;

    alter table drug_family 
        drop 
        foreign key FKD5F254E344975E15;

    alter table drug_synonym 
        drop 
        foreign key FKC10E725C44975E15;

    alter table evidence 
        drop 
        foreign key FK16D39E57C602BC92;

    alter table evidence_alteration 
        drop 
        foreign key FKCF414387AAD5975;

    alter table evidence_alteration 
        drop 
        foreign key FKCF414387F7262935;

    alter table evidence_article 
        drop 
        foreign key FK947FCB0E981D93BF;

    alter table evidence_article 
        drop 
        foreign key FK947FCB0EF7262935;

    alter table evidence_cancer_type 
        drop 
        foreign key FKE65A5BB14C5916CF;

    alter table evidence_cancer_type 
        drop 
        foreign key FKE65A5BB1F7262935;

    alter table evidence_excluded_cancer_type 
        drop 
        foreign key FK3FC7AF8C4C5916CF;

    alter table evidence_excluded_cancer_type 
        drop 
        foreign key FK3FC7AF8CF7262935;

    alter table evidence_relevant_cancer_type 
        drop 
        foreign key FK20D24FDF4C5916CF;

    alter table evidence_relevant_cancer_type 
        drop 
        foreign key FK20D24FDFF7262935;

    alter table gene_alias 
        drop 
        foreign key FKF1C8F0A6C602BC92;

    alter table geneset_gene 
        drop 
        foreign key FKA19C3AE7C602BC92;

    alter table geneset_gene 
        drop 
        foreign key FKA19C3AE7C94E945F;

    alter table portal_alteration 
        drop 
        foreign key FK29E0AA52C602BC92;

    alter table portal_alteration_oncokb_alteration 
        drop 
        foreign key FKB822F32F7BF2942E;

    alter table portal_alteration_oncokb_alteration 
        drop 
        foreign key FKB822F32F56653D18;

    alter table treatment 
        drop 
        foreign key FKFC397878F7262935;

    alter table treatment_approved_indications 
        drop 
        foreign key FK333C46769D586F3F;

    alter table treatment_drug 
        drop 
        foreign key FK5720FB0744975E15;

    alter table treatment_drug 
        drop 
        foreign key FK5720FB079D586F3F;

    drop table if exists alteration;

    drop table if exists alteration_reference_genome;

    drop table if exists article;

    drop table if exists cancer_type;

    drop table if exists cancer_type_child;

    drop table if exists cancer_type_synonym;

    drop table if exists drug;

    drop table if exists drug_family;

    drop table if exists drug_synonym;

    drop table if exists evidence;

    drop table if exists evidence_alteration;

    drop table if exists evidence_article;

    drop table if exists evidence_cancer_type;

    drop table if exists evidence_excluded_cancer_type;

    drop table if exists evidence_relevant_cancer_type;

    drop table if exists gene;

    drop table if exists gene_alias;

    drop table if exists geneset;

    drop table if exists geneset_gene;

    drop table if exists info;

    drop table if exists portal_alteration;

    drop table if exists portal_alteration_oncokb_alteration;

    drop table if exists treatment;

    drop table if exists treatment_approved_indications;

    drop table if exists treatment_drug;

    drop table if exists variant_consequence;

    create table alteration (
        id integer not null auto_increment,
        alteration longtext,
        alteration_type varchar(255),
        for_germline bit,
        name longtext,
        protein_change longtext,
        protein_end integer,
        protein_start integer,
        ref_residues varchar(255),
        uuid varchar(40),
        variant_residues varchar(255),
        consequence varchar(100),
        entrez_gene_id integer,
        primary key (id)
    );

    create table alteration_reference_genome (
        alteration_id integer not null,
        reference_genome varchar(10)
    );

    create table article (
        id integer not null auto_increment,
        abstract_content varchar(255),
        authors varchar(255),
        elocationId varchar(255),
        issue varchar(255),
        journal varchar(255),
        link longtext,
        pages varchar(255),
        pmid varchar(255),
        pub_date varchar(255),
        title longtext,
        uuid varchar(40),
        volume varchar(255),
        primary key (id)
    );

    create table cancer_type (
        id integer not null auto_increment,
        code varchar(255) not null,
        color varchar(255) not null,
        level integer not null,
        main_type varchar(255) not null,
        subtype varchar(255) not null,
        tissue varchar(255) not null,
        tumor_form varchar(255),
        parent integer,
        primary key (id)
    );

    create table cancer_type_child (
        cancer_type_id integer not null,
        cancer_type_child_id integer not null,
        primary key (cancer_type_id, cancer_type_child_id)
    );

    create table cancer_type_synonym (
        id integer not null auto_increment,
        code varchar(255) not null,
        main_type varchar(255) not null,
        subtype varchar(255) not null,
        cancer_type_id integer,
        primary key (id)
    );

    create table drug (
        id integer not null auto_increment,
        description longtext,
        drug_name longtext not null,
        ncit_code varchar(20),
        type varchar(20),
        uuid varchar(40),
        primary key (id)
    );

    create table drug_family (
        drug_id integer not null,
        drug_family_id integer not null,
        primary key (drug_id, drug_family_id)
    );

    create table drug_synonym (
        drug_id integer not null,
        synonym longtext
    );

    create table evidence (
        id integer not null auto_increment,
        additional_info longtext,
        description longtext,
        evidence_type varchar(255),
        fda_level varchar(255),
        for_germline bit,
        known_effect varchar(255),
        last_edit datetime,
        last_review datetime,
        level_of_evidence varchar(255),
        liquid_propagation_level varchar(255),
        name longtext,
        solid_propagation_level varchar(255),
        uuid varchar(40),
        entrez_gene_id integer,
        primary key (id)
    );

    create table evidence_alteration (
        evidence_id integer not null,
        alteration_id integer not null,
        primary key (evidence_id, alteration_id)
    );

    create table evidence_article (
        evidence_id integer not null,
        article_id integer not null,
        primary key (evidence_id, article_id)
    );

    create table evidence_cancer_type (
        evidence_id integer not null,
        cancer_type_id integer not null,
        primary key (evidence_id, cancer_type_id)
    );

    create table evidence_excluded_cancer_type (
        evidence_id integer not null,
        cancer_type_id integer not null,
        primary key (evidence_id, cancer_type_id)
    );

    create table evidence_relevant_cancer_type (
        evidence_id integer not null,
        cancer_type_id integer not null,
        primary key (evidence_id, cancer_type_id)
    );

    create table gene (
        entrez_gene_id integer not null,
        gene_type varchar(30),
        grch37_isoform varchar(100),
        grch37_ref_seq varchar(100),
        grch38_isoform varchar(100),
        grch38_ref_seq varchar(100),
        hugo_symbol varchar(50) unique,
        primary key (entrez_gene_id)
    );

    create table gene_alias (
        entrez_gene_id integer not null,
        alias varchar(255)
    );

    create table geneset (
        id integer not null auto_increment,
        name varchar(255) not null,
        uuid varchar(40) not null,
        primary key (id)
    );

    create table geneset_gene (
        geneset_id integer not null,
        entrez_gene_id integer not null,
        primary key (geneset_id, entrez_gene_id)
    );

    create table info (
        id integer not null auto_increment,
        data_version varchar(255),
        data_version_date datetime,
        ncit_version varchar(255),
        oncotree_version varchar(255),
        primary key (id)
    );

    create table portal_alteration (
        id integer not null auto_increment,
        alteration_type longtext,
        cancer_study longtext not null,
        cancer_type longtext not null,
        protein_change varchar(255),
        protein_end integer,
        protein_start integer,
        sample_id varchar(255),
        entrez_gene_id integer,
        primary key (id)
    );

    create table portal_alteration_oncokb_alteration (
        oncokb_alteration_id integer not null,
        portal_alteration_id integer not null,
        primary key (oncokb_alteration_id, portal_alteration_id)
    );

    create table treatment (
        id integer not null auto_increment,
        priority integer,
        uuid varchar(40),
        evidence_id integer,
        primary key (id)
    );

    create table treatment_approved_indications (
        treatment_id integer not null,
        approved_indications longtext
    );

    create table treatment_drug (
        priority integer,
        drug_id integer,
        treatment_id integer,
        primary key (drug_id, treatment_id)
    );

    create table variant_consequence (
        term varchar(100) not null,
        description varchar(255) not null,
        is_generally_truncating bit not null,
        primary key (term)
    );

    alter table alteration 
        add index FKD5A6189FC602BC92 (entrez_gene_id), 
        add constraint FKD5A6189FC602BC92 
        foreign key (entrez_gene_id) 
        references gene (entrez_gene_id);

    alter table alteration 
        add index FKD5A6189FAEE285DC (consequence), 
        add constraint FKD5A6189FAEE285DC 
        foreign key (consequence) 
        references variant_consequence (term);

    alter table alteration_reference_genome 
        add index FKE8AFDC0BAAD5975 (alteration_id), 
        add constraint FKE8AFDC0BAAD5975 
        foreign key (alteration_id) 
        references alteration (id);

    alter table cancer_type 
        add index FKAC902999B4A9E9F8 (parent), 
        add constraint FKAC902999B4A9E9F8 
        foreign key (parent) 
        references cancer_type (id);

    alter table cancer_type_child 
        add index FK25672AF690C0BC52 (cancer_type_child_id), 
        add constraint FK25672AF690C0BC52 
        foreign key (cancer_type_child_id) 
        references cancer_type (id);

    alter table cancer_type_child 
        add index FK25672AF64C5916CF (cancer_type_id), 
        add constraint FK25672AF64C5916CF 
        foreign key (cancer_type_id) 
        references cancer_type (id);

    alter table cancer_type_synonym 
        add index FKD3F663554C5916CF (cancer_type_id), 
        add constraint FKD3F663554C5916CF 
        foreign key (cancer_type_id) 
        references cancer_type (id);

    alter table drug_family 
        add index FKD5F254E34E48DF2 (drug_family_id), 
        add constraint FKD5F254E34E48DF2 
        foreign key (drug_family_id) 
        references drug (id);

    alter table drug_family 
        add index FKD5F254E344975E15 (drug_id), 
        add constraint FKD5F254E344975E15 
        foreign key (drug_id) 
        references drug (id);

    alter table drug_synonym 
        add index FKC10E725C44975E15 (drug_id), 
        add constraint FKC10E725C44975E15 
        foreign key (drug_id) 
        references drug (id);

    alter table evidence 
        add index FK16D39E57C602BC92 (entrez_gene_id), 
        add constraint FK16D39E57C602BC92 
        foreign key (entrez_gene_id) 
        references gene (entrez_gene_id);

    alter table evidence_alteration 
        add index FKCF414387AAD5975 (alteration_id), 
        add constraint FKCF414387AAD5975 
        foreign key (alteration_id) 
        references alteration (id);

    alter table evidence_alteration 
        add index FKCF414387F7262935 (evidence_id), 
        add constraint FKCF414387F7262935 
        foreign key (evidence_id) 
        references evidence (id);

    alter table evidence_article 
        add index FK947FCB0E981D93BF (article_id), 
        add constraint FK947FCB0E981D93BF 
        foreign key (article_id) 
        references article (id);

    alter table evidence_article 
        add index FK947FCB0EF7262935 (evidence_id), 
        add constraint FK947FCB0EF7262935 
        foreign key (evidence_id) 
        references evidence (id);

    alter table evidence_cancer_type 
        add index FKE65A5BB14C5916CF (cancer_type_id), 
        add constraint FKE65A5BB14C5916CF 
        foreign key (cancer_type_id) 
        references cancer_type (id);

    alter table evidence_cancer_type 
        add index FKE65A5BB1F7262935 (evidence_id), 
        add constraint FKE65A5BB1F7262935 
        foreign key (evidence_id) 
        references evidence (id);

    alter table evidence_excluded_cancer_type 
        add index FK3FC7AF8C4C5916CF (cancer_type_id), 
        add constraint FK3FC7AF8C4C5916CF 
        foreign key (cancer_type_id) 
        references cancer_type (id);

    alter table evidence_excluded_cancer_type 
        add index FK3FC7AF8CF7262935 (evidence_id), 
        add constraint FK3FC7AF8CF7262935 
        foreign key (evidence_id) 
        references evidence (id);

    alter table evidence_relevant_cancer_type 
        add index FK20D24FDF4C5916CF (cancer_type_id), 
        add constraint FK20D24FDF4C5916CF 
        foreign key (cancer_type_id) 
        references cancer_type (id);

    alter table evidence_relevant_cancer_type 
        add index FK20D24FDFF7262935 (evidence_id), 
        add constraint FK20D24FDFF7262935 
        foreign key (evidence_id) 
        references evidence (id);

    alter table gene_alias 
        add index FKF1C8F0A6C602BC92 (entrez_gene_id), 
        add constraint FKF1C8F0A6C602BC92 
        foreign key (entrez_gene_id) 
        references gene (entrez_gene_id);

    alter table geneset_gene 
        add index FKA19C3AE7C602BC92 (entrez_gene_id), 
        add constraint FKA19C3AE7C602BC92 
        foreign key (entrez_gene_id) 
        references gene (entrez_gene_id);

    alter table geneset_gene 
        add index FKA19C3AE7C94E945F (geneset_id), 
        add constraint FKA19C3AE7C94E945F 
        foreign key (geneset_id) 
        references geneset (id);

    alter table portal_alteration 
        add index FK29E0AA52C602BC92 (entrez_gene_id), 
        add constraint FK29E0AA52C602BC92 
        foreign key (entrez_gene_id) 
        references gene (entrez_gene_id);

    alter table portal_alteration_oncokb_alteration 
        add index FKB822F32F7BF2942E (portal_alteration_id), 
        add constraint FKB822F32F7BF2942E 
        foreign key (portal_alteration_id) 
        references portal_alteration (id);

    alter table portal_alteration_oncokb_alteration 
        add index FKB822F32F56653D18 (oncokb_alteration_id), 
        add constraint FKB822F32F56653D18 
        foreign key (oncokb_alteration_id) 
        references alteration (id);

    alter table treatment 
        add index FKFC397878F7262935 (evidence_id), 
        add constraint FKFC397878F7262935 
        foreign key (evidence_id) 
        references evidence (id);

    alter table treatment_approved_indications 
        add index FK333C46769D586F3F (treatment_id), 
        add constraint FK333C46769D586F3F 
        foreign key (treatment_id) 
        references treatment (id);

    alter table treatment_drug 
        add index FK5720FB0744975E15 (drug_id), 
        add constraint FK5720FB0744975E15 
        foreign key (drug_id) 
        references drug (id);

    alter table treatment_drug 
        add index FK5720FB079D586F3F (treatment_id), 
        add constraint FK5720FB079D586F3F 
        foreign key (treatment_id) 
        references treatment (id);
